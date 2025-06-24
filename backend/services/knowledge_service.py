import os

from flask import jsonify
from supabase import create_client
from utils.supabase_client import get_supabase

metadata_file = "file_records.json"
UPLOAD_FOLDER = "uploads"

import json
def load_metadata():
    if not os.path.exists(metadata_file):
        return []
    with open(metadata_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_metadata(data):
    with open(metadata_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def save_file_metadata(name, path, user_id=None):
    data = load_metadata()
    record = {"filename": name, "path": path}
    if user_id is not None:
        record["user_id"] = user_id
    data.append(record)
    save_metadata(data)
    # supabase = get_supabase()
    # try:
    #     insert_result = supabase.table("files").insert({
    #         "user_id": user_id,
    #         "filename": name,
    #         "filepath": path
    #     }).execute()
    #     file_id = insert_result.data[0]["id"]  # ✅ 获取外键
    # except Exception as e:
    #     return jsonify({"error": f"插入文件记录失败：{str(e)}"}), 500

def delete_file_metadata(filename):
    data = load_metadata()
    data = [f for f in data if f["filename"] != filename]
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    if os.path.exists(file_path):
        os.remove(file_path)
    save_metadata(data)
    return {"message": "删除成功"}

def get_all_files():
    return {"files": load_metadata()}

def demo():
    print(1)

def delete_file_metadata(filename):
    data = load_metadata()
    target = next((f for f in data if f["filename"] == filename), None)
    data = [f for f in data if f["filename"] != filename]

    if target:
        path = target["path"]
        # 🧠 删除 Supabase 向量
        supabase.table("user_vectors")\
            .delete()\
            .eq("file_path", path)\
            .execute()

        if os.path.exists(path):
            os.remove(path)

    save_metadata(data)
    return {"message": "删除成功"}
