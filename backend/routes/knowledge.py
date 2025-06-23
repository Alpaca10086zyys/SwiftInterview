from flask import Blueprint, request, jsonify, send_from_directory
import os
from werkzeug.utils import secure_filename
from services.knowledge_service import save_file_metadata, delete_file_metadata, get_all_files
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

    filename = secure_filename(file.filename)
    name, ext = os.path.splitext(filename)
    # 最终文件名再拼接上当前时间戳
    timestamp = int(time.time())
    final_filename = f"{name}_{timestamp}{ext}"
    file_path = os.path.join(user_folder, final_filename)
    file.save(file_path)

    save_file_metadata(final_filename, file_path, user_id)

    return jsonify({"message": "上传成功", "filename": final_filename, "user_id": user_id})

@knowledge_bp.route('/knowledge/delete', methods=['POST'])
def delete_file():
    data = request.get_json()
    filename = data.get('filename')

    if not filename:
        return jsonify({"error": "缺少文件名"}), 400

    result = delete_file_metadata(filename)
    return jsonify(result)

@knowledge_bp.route('/knowledge/list', methods=['GET'])
def list_files():
    return jsonify(get_all_files())

@knowledge_bp.route('/knowledge/download/<filename>', methods=['GET'])
def download_file(filename):
    return send_from_directory(UPLOAD_FOLDER, filename, as_attachment=True)
