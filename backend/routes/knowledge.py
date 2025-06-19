from flask import Blueprint, request, jsonify, send_from_directory
import os
from werkzeug.utils import secure_filename
from services.knowledge_service import save_file_metadata, delete_file_metadata, get_all_files

knowledge_bp = Blueprint('knowledge', __name__)
UPLOAD_FOLDER = "uploads"
ALLOWED_EXTENSIONS = {'pdf', 'docx', 'txt', 'png', 'jpg', 'jpeg'}

os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@knowledge_bp.route('/knowledge/upload', methods=['POST'])
def upload_file():
    file = request.files.get('file')
    if not file or not allowed_file(file.filename):
        return jsonify({"error": "文件类型不允许"}), 400

    filename = secure_filename(file.filename)
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    file.save(file_path)

    save_file_metadata(filename, file_path)

    return jsonify({"message": "上传成功", "filename": filename})

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
