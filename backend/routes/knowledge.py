from flask import Blueprint, request, jsonify, send_from_directory
import os
from werkzeug.utils import secure_filename
from services.knowledge_service import save_file_metadata, delete_file_metadata, get_all_files
from utils.embedding.text_embedder import embed_txt_file,get_embedding
from utils.supabase_client import get_supabase
import time

knowledge_bp = Blueprint('knowledge', __name__, url_prefix="/api/knowledge")
UPLOAD_FOLDER = "uploads"
ALLOWED_EXTENSIONS = {'pdf', 'docx', 'txt', 'png', 'jpg', 'jpeg','json'}

os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@knowledge_bp.route('/upload', methods=['POST'])
def upload_file():
    """
    客户端上传文件到服务端，json格式：
    {
        "file":<上传的文件>,
        "user_id":<当前客户端用户id>
    }
    """
    file = request.files.get('file')
    user_id = request.form.get('user_id')

    if not file:
        return jsonify({"error": "文件为空"}), 400
    elif not allowed_file(file.filename):
        return jsonify({"error": "文件类型不允许"}), 400
    if not user_id:
        return jsonify({"error": "缺少用户ID"}), 400

    # 创建用户专属文件夹:user_id
    user_folder = os.path.join(UPLOAD_FOLDER, user_id)
    os.makedirs(user_folder, exist_ok=True)

    filename = file.filename
    name, ext = os.path.splitext(filename)
    # 最终文件名再拼接上当前时间戳
    timestamp = int(time.time())
    final_filename = f"{name}_{timestamp}{ext}"
    file_path = os.path.join(user_folder, final_filename)
    file.save(file_path)

    # save_file_metadata(final_filename, file_path, user_id)

    supabase = get_supabase()
    try:
        insert_result = supabase.table("files").insert({
            "user_id": user_id,
            "filename": filename,
            "filepath": file_path
        }).execute()
        file_id = insert_result.data[0]["id"]  # ✅ 获取外键
    except Exception as e:
        return jsonify({"error": f"插入文件记录失败：{str(e)}"})
    # 🌟 添加这句：向量化
    if ext.lower() == ".txt":
        try:
            embed_txt_file(file_path=file_path, file_id=file_id)  # ✅ 新版函数只需 file_path + file_id
        except Exception as e:
            return jsonify({"error": f"向量化失败：{str(e)}"}), 500

    return jsonify({
        "message": "上传成功",
        "file_id": file_id,
        "filename": final_filename,
        "user_id": user_id
    })

@knowledge_bp.route('/delete', methods=['POST'])
def delete_file():
    """
    请求格式：
    {
        "file_id": 123,
        "user_id": "abc123"
    }
    """
    data = request.get_json()
    file_id = data.get("file_id")
    user_id = data.get("user_id")

    if not (file_id and user_id):
        return jsonify({"error": "缺少参数"}), 400

    supabase = get_supabase()

    # 1. 获取文件路径
    try:
        res = supabase.table("files").select("filepath").eq("id", file_id).single().execute()
        if not res.data:
            return jsonify({"error": "文件记录不存在"}), 404
        filepath = res.data["filepath"]
    except Exception as e:
        return jsonify({"error": f"查询文件失败：{str(e)}"}), 500

    # 2. 删除本地文件
    try:
        if os.path.exists(filepath):
            os.remove(filepath)
        else:
            print(f"[⚠️] 本地文件不存在：{filepath}")
    except Exception as e:
        return jsonify({"error": f"删除本地文件失败：{str(e)}"}), 500

    # 3. 删除 Supabase 中的文件记录（自动级联删除 user_vectors）
    try:
        delete_res = supabase.table("files").delete().eq("id", file_id).execute()
    except Exception as e:
        return jsonify({"error": f"删除数据库记录失败：{str(e)}"}), 500

    return jsonify({
        "message": "删除成功",
        "file_id": file_id,
        "user_id": user_id
    })


@knowledge_bp.route('/list', methods=['GET'])
def list_files():
    """
     前端调用示例：
        GET /api/knowledge/list?user_id=abc123
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
            .table("files")
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

@knowledge_bp.route('/download', methods=['GET'])
def download_file():
    """
    根据 file_id 从 Supabase 查询文件路径，然后返回文件
    """
    file_id = request.args.get("file_id")
    supabase = get_supabase()

    try:
        # 1. 查询 Supabase 数据库，获取该文件的路径和原始文件名
        res = supabase.table("files").select("filepath, filename").eq("id", file_id).single().execute()
        if not res.data:
            return jsonify({"error": "找不到该文件"}), 404

        filepath = res.data["filepath"]
        filename = res.data["filename"]  # 可用于作为下载时的原名
    except Exception as e:
        return jsonify({"error": f"数据库查询失败: {str(e)}"}), 500

    # 2. 确保文件存在
    if not os.path.exists(filepath):
        return jsonify({"error": f"文件不存在: {filepath}"}), 404

    # 3. 计算目录与文件名
    directory = os.path.dirname(filepath)
    final_filename = os.path.basename(filepath)

    # 4. 通过 Flask 内置的 send_from_directory 发送
    return send_from_directory(directory, final_filename, as_attachment=True, download_name=filename)


@knowledge_bp.route('/search', methods=['POST'])
def search_files():
    """
    请求格式：
    {
        "query": "搜索关键词",
        "user_id": "用户ID",
        "threshold": 0.7,  # 可选，相似度阈值
        "top_k": 5         # 可选，返回结果数
    }
    """
    data = request.get_json()
    query = data.get("query")
    user_id = data.get("user_id")
    threshold = data.get("threshold", 0.7)
    top_k = data.get("top_k", 5)

    if not query or not user_id:
        return jsonify({"error": "缺少必要参数"}), 400

    # 获取查询文本的嵌入向量
    try:
        query_embedding = get_embedding(query)
    except Exception as e:
        return jsonify({"error": f"向量化失败: {str(e)}"}), 500

    # 执行 Supabase 搜索
    supabase = get_supabase()
    try:
        result = supabase.rpc('search_vectors', {
            'user_id': user_id,
            'query_embedding': query_embedding,
            'match_threshold': threshold,
            'match_count': top_k
        }).execute()

        return jsonify({
            "results": result.data,
            "query": query,
            "user_id": user_id
        })
    except Exception as e:
        return jsonify({"error": f"搜索失败: {str(e)}"}), 500