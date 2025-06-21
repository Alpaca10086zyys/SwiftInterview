from flask import Blueprint, request, jsonify
from services.user_service import insert_user, delete_user

user_bp = Blueprint("user", __name__, url_prefix="/api/user")

@user_bp.route("/add_user", methods=["POST"])
def add_user_route():
    status, result = insert_user(request.json)
    return jsonify(result), status

@user_bp.route("/delete_user/<user_id>", methods=["DELETE"])
def delete_user_route(user_id):
    status, result = delete_user(user_id)
    return jsonify(result), status
