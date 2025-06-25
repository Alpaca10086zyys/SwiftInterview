from flask import Blueprint, request, jsonify, send_from_directory
import os
from werkzeug.utils import secure_filename
from services.knowledge_service import save_file_metadata, delete_file_metadata, get_all_files
from utils.embedding.text_embedder import embed_txt_file,get_embedding
from utils.supabase_client import get_supabase
import time

knowledge_bp = Blueprint('knowledge', __name__)
UPLOAD_FOLDER = "uploads"
ALLOWED_EXTENSIONS = {'pdf', 'docx', 'txt', 'png', 'jpg', 'jpeg','json'}

os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@knowledge_bp.route('/knowledge/upload', methods=['POST'])
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

    save_file_metadata(final_filename, file_path, user_id)

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

@knowledge_bp.route('/knowledge/delete', methods=['POST'])
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


@knowledge_bp.route('/knowledge/list', methods=['GET'])
def list_files():
    return jsonify(get_all_files())

@knowledge_bp.route('/knowledge/download/<filename>', methods=['GET'])
def download_file(filename):
    return send_from_directory(UPLOAD_FOLDER, filename, as_attachment=True)

@knowledge_bp.route('/knowledge/search', methods=['POST'])
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