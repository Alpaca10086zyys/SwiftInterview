from flask import Blueprint, request, jsonify
from services.review_service import *
from utils.supabase_client import get_supabase

review_bp = Blueprint("review", __name__, url_prefix="/api/review")

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
