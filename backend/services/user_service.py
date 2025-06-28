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
