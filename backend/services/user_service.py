import requests
from flask import current_app

def insert_user(data):
    url = f"{current_app.config['SUPABASE_URL']}/rest/v1/users"
    headers = {
        "apikey": current_app.config["SUPABASE_KEY"],
        "Authorization": f"Bearer {current_app.config['SUPABASE_KEY']}",
        "Content-Type": "application/json",
        "Prefer": "return=representation"
    }
    res = requests.post(url, headers=headers, json=data)
    try:
        json_data = res.json()
    except Exception:
        json_data = None
    return res.status_code, json_data


def delete_user(user_id):
    url = f"{current_app.config['SUPABASE_URL']}/rest/v1/users?id=eq.{user_id}"
    headers = {
        "apikey": current_app.config["SUPABASE_KEY"],
        "Authorization": f"Bearer {current_app.config['SUPABASE_KEY']}"
    }
    res = requests.delete(url, headers=headers)
    return res.status_code, res.text if res.status_code != 204 else {"message": "Deleted"}


def login_user(email, password):
    url = f"{current_app.config['SUPABASE_URL']}/rest/v1/users?email=eq.{email}&select=*"
    headers = {
        "apikey": current_app.config["SUPABASE_KEY"],
        "Authorization": f"Bearer {current_app.config['SUPABASE_KEY']}",
    }

    res = requests.get(url, headers=headers)

    try:
        users = res.json()
        print(users)
    except Exception:
        return 500, {
            "message": "Invalid Supabase response"
        }

    if res.status_code != 200:
        return res.status_code, {"message": "Failed to query Supabase"}

    if not users:
        return 404, {"message": "User not found"}

    user = users[0]
    if user["password"] != password:
        return 401, {"message": "Incorrect password"}

    return 200, {
        "id": user["id"],
        "created_at": user["created_at"],
        "email": user["email"],
        "user_name": user["user_name"],
        "job_status": user["job_status"],
        "message": "Login successful"
    }


def update_job_status(user_id, new_status):
    url = f"{current_app.config['SUPABASE_URL']}/rest/v1/users?id=eq.{user_id}"
    headers = {
        "apikey": current_app.config["SUPABASE_KEY"],
        "Authorization": f"Bearer {current_app.config['SUPABASE_KEY']}",
        "Content-Type": "application/json",
        "Prefer": "return=representation"
    }
    data = {"job_status": new_status}
    res = requests.patch(url, headers=headers, json=data)
    try:
        json_data = res.json()
    except Exception:
        json_data = None
    return res.status_code, json_data

