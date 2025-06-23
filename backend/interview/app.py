from flask import Flask, request, jsonify
from ai_interview3 import ask_one_question
from user_audio import generate_audio_from_text
from asr import ai_speech

app = Flask(__name__)

# 声明 base_prompt 为全局变量
base_prompt = ""

@app.route('/modify-text', methods=['POST'])
def modify_text():
    global base_prompt  # 使用 global 关键字修改全局变量
    # 从请求中获取指令
    data = request.json
    job_title = data.get("job_title")
    style = data.get("style")
    imp = data.get("imp")
    base_prompt = f"这是一场针对{job_title}岗位的面试，你作为面试官，你的风格是{style},你的关注重点是{imp},请你开始问问题"
    return base_prompt

@app.route('/start_interview', methods=['POST'])
def start_interview():
    global base_prompt  # 使用 global 关键字访问和修改全局变量
    first_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(first_question)

    # 返回第一个问题和音频URL
    return jsonify({
        'next_question': first_question,
        'audio_url': audio  # 假设音频文件保存为静态文件，供前端播放
    })

@app.route('/upload_audio', methods=['POST'])
def upload_audio():
    global base_prompt  # 使用 global 关键字访问和修改全局变量
    if 'file' not in request.files:
        return jsonify({"error": "No file part"})

    file = request.files['file']
    input_list = ai_speech.recognize_wav(file)
    base_prompt = f"{base_prompt}, 上一个问题用户的回答是{input_list}"
    last_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(last_question)
    return jsonify({
        'user_response':input_list,
        'next_question': last_question,
        'audio_url': audio  # 假设音频文件保存为静态文件，供前端播放
    })

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
