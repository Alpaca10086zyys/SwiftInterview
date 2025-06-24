import os
from openai import OpenAI
from langchain.text_splitter import RecursiveCharacterTextSplitter
from sentence_transformers import SentenceTransformer
from supabase import create_client
from flask import current_app
from utils.supabase_client import get_supabase


# === 加载中文向量模型 ===
model = SentenceTransformer("BAAI/bge-small-zh")

# ==== 提取向量 ====
def get_embedding(text):
    return model.encode(text, normalize_embeddings=True).tolist()


# ==== 向量化文件 ====
def embed_txt_file(file_path: str, file_id: int):
    supabase = get_supabase()
    # 👇 获取 user_id
    file_record = supabase.table("files").select("user_id").eq("id", file_id).single().execute()
    user_id = file_record.data["user_id"]
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    splitter = RecursiveCharacterTextSplitter(
        chunk_size=500,
        chunk_overlap=50,
        separators=["\n\n", "\n", "。", "！", "？", "，", " ", ""]
    )
    chunks = splitter.split_text(text)


    for i, chunk in enumerate(chunks):
        try:
            embedding = get_embedding(chunk)
            supabase.table("user_vectors").insert({
                "file_id": file_id,  # ✅ 用外键指向 files 表
                "user_id": user_id,
                "content": chunk,
                "embedding": embedding,
                "metadata": {
                    "chunk_index": i
                }
            }).execute()
            print(f"[✅] 插入 file_id={file_id} 第 {i + 1} 段")
        except Exception as e:
            print(f"[❌] 插入失败（file_id={file_id}, chunk={i + 1}）：{e}")
