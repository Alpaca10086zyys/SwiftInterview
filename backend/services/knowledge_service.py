import os

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

def save_file_metadata(name, path):
    data = load_metadata()
    data.append({"filename": name, "path": path})
    save_metadata(data)

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
