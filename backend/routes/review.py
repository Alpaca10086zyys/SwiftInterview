import threading

from flask import Blueprint, request, jsonify, current_app
from services.review_service import run_pipeline
from utils.supabase_client import get_supabase
from routes.knowledge import allowed_file
import os
from pydub import AudioSegment
import time
from routes.interview_audio.asr.ai_speech import recognize_wav
import speech_recognition as sr

review_bp = Blueprint("review", __name__, url_prefix="/api/review")
ALLOWED_EXTENSIONS = {'wav', 'mp3', 'ogg', 'flac', 'aac', 'm4a', 'mp4', 'wma', 'aiff'}
# 配置上传文件夹
UPLOAD_FOLDER = 'uploads_audio'

def fire_and_forget(user_id, title, raw_file_path, duration, raw_text, tags=None):
    from app import create_app

    app = create_app()  # ✅ 注意这里重新获取 app 实例

    with app.app_context():  # 确保在 Flask 上下文中运行
        try:
            review_content = run_pipeline(raw_text)
            supabase = get_supabase()

            # 构建插入数据字典
            insert_data = {
                "user_id": user_id,
                "title": title,
                "raw_file_path": raw_file_path,
                "review_content": review_content,
                "duration": duration
            }

            # 如果传入了 tags，就添加进字典
            if tags is not None:
                insert_data["tags"] = tags

            # 插入数据
            res = (
                supabase
                .table("interview_list")
                .insert(insert_data)
                .execute()
            )

            if res.status_code != 200:
                print("插入失败:", res.json())  # 获取错误信息
            else:
                print("插入成功:", res.data)
        except Exception as e:
            print("评估失败：", e)


@review_bp.route("/list", methods=["GET"])
def list_files():
    """
     前端调用示例：
        GET /api/review/list?user_id=abc123
     返回：
        [
          {
            "id": 17,
            "user_id": "abc123",
            "filename": "note_1729999999.txt",
            "filepath": "uploads/abc123/note_1729999999.txt",
            "created_at": "2025-06-30T14:22:10.123456+00:00"
          },
          ...
        ]
    :return:
    """
    # 1) 取查询参数
    user_id = request.args.get("user_id")
    if not user_id:
        return jsonify({"error": "缺少 user_id"}), 400

    supabase = get_supabase()

    try:
        # 2) 查询 files 表中属于该用户的全部记录
        #    按 created_at 升序；如需倒序改为 desc=True
        res = (
            supabase
            .table("interview_list")
            .select("*")
            .eq("user_id", user_id)
            .order("created_at")  # ➜ 按时间排序，默认 asc=True
            .execute()
        )
        data = res.data or []  # res.data 为空时返回 []
    except Exception as e:
        return jsonify({"error": f"查询失败：{str(e)}"}), 500

    # 3) 直接把查询结果作为 JSON 发回前端
    return jsonify(data), 200


@review_bp.route('/upload_audio', methods=['POST'])
def upload_audio():
    """
        客户端上传文件到服务端，json格式：
        {
            "file":<上传的文件>,
            "user_id":<当前客户端用户id>
        }
    """

    audio_file = request.files.get('file')
    user_id = request.form.get('user_id')

    # 验证json
    if not audio_file:
        return jsonify({"error": "文件为空"}), 400
    elif audio_file.filename == '':
        return jsonify({"error": "文件名为空"}), 400
    elif not allowed_file(audio_file.filename,ALLOWED_EXTENSIONS):
        print(audio_file.filename)
        return jsonify({"error": "文件类型不允许"}), 400
    if not user_id:
        return jsonify({"error": "缺少用户ID"}), 400

    try:
        # 保存上传的文件
        # 创建用户专属文件夹:user_id
        user_folder = os.path.join(UPLOAD_FOLDER, user_id)
        os.makedirs(user_folder, exist_ok=True)

        filename = audio_file.filename
        name, ext = os.path.splitext(filename)
        # 最终文件名再拼接上当前时间戳
        timestamp = int(time.time())
        final_filename = f"{name}_{timestamp}{ext}"
        file_path = os.path.join(user_folder, final_filename)
        audio_file.save(file_path)

        # 检查文件扩展名，如果是wav则直接使用，否则转换
        if ext.lower() == '.wav':
            wav_path = file_path
            audio = AudioSegment.from_file(file_path)  # 加载 WAV 文件
        else:
            # 将文件转换为WAV格式（兼容处理）
            audio = AudioSegment.from_file(file_path)
            wav_path = os.path.join(user_folder, f"{name}_{timestamp}.wav")
            audio.export(wav_path, format="wav")

        # ✅ 计算音频时长（毫秒转小时、分钟、秒）
        total_seconds = int(audio.duration_seconds)
        duration = {
            "h": total_seconds // 3600,
            "m": (total_seconds % 3600) // 60,
            "s": total_seconds % 60
        }

        # 使用 speech_recognition 做识别
        recognizer = sr.Recognizer()
        with sr.AudioFile(wav_path) as source:
            audio_data = recognizer.record(source)
            text = recognizer.recognize_google(audio_data, language='zh-CN')  # 中文识别

        try:
            threading.Thread(
                target=fire_and_forget,
                args=(user_id, filename, file_path, duration, text),
                daemon=True
            ).start()

            return jsonify({
                "status": "accepted",
                "message": "音频文件已上传并正在处理",
                "audio_path": wav_path,
            })

        except Exception as e:
            # 启动线程失败，比如参数错误或系统资源问题
            return jsonify({
                "status": "error",
                "message": f"后台任务启动失败：{str(e)}"
            }), 500

    except Exception as e:
        return jsonify({"error": str(e)}), 500
