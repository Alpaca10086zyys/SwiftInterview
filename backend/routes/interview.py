from flask import Flask, request, jsonify,send_from_directory
from interview_audio.ai_interview3 import ask_one_question
from interview_audio.user_audio import generate_audio_from_text
from interview_audio.asr import ai_speech
from interview_audio.prompt import get_depth_description
from interview_audio.prompt import get_jumpiness_description
import os
from datetime import datetime


app = Flask(__name__, static_folder="interview_audio/static", static_url_path="")
app.config['OUTPUT_AUDIO_FOLDER'] = 'output_audio'
if not os.path.exists(app.config['OUTPUT_AUDIO_FOLDER']):
    os.makedirs(app.config['OUTPUT_AUDIO_FOLDER'])

app.config['INTERVIEW_LOG_FOLDER'] = 'txt'
if not os.path.exists(app.config['INTERVIEW_LOG_FOLDER']):
    os.makedirs(app.config['INTERVIEW_LOG_FOLDER'])

log_file_path = ""  # 记录当前会话日志文件路径


@app.route('/')
def index():
    return app.send_static_file('index_text.html')  # 保证 index.html 是静态文件夹里的


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

@app.route('/modify-text_new', methods=['POST'])
def modify_text_new():
    global base_prompt  # 使用 global 关键字修改全局变量
    # 从请求中获取指令
    data = request.json
    job_title = data.get("job_title")
    style = data.get("style")
    imp = data.get("imp")
    jumpiness_level = data.get("jumpiness_level")  # 获取思维跳跃程度
    question_count = data.get("depth")
    jumpiness = get_jumpiness_description(jumpiness_level)
    depth = get_depth_description(question_count)
    base_prompt = f"这是一场针对{job_title}岗位的面试，你作为面试官，你的风格是{style},你的关注重点是{imp},思维跳跃程度是{jumpiness},问题深度是{depth},请你开始问问题，只能询问一个问题"
    return base_prompt

@app.route('/start_interview', methods=['POST'])
def start_interview():
    global base_prompt, log_file_path

    first_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(first_question)

    # 生成日志文件名：面试+时间.txt
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"面试_{timestamp}.txt"
    log_file_path = os.path.join(app.config['INTERVIEW_LOG_FOLDER'], filename)

    # 记录第一条问题
    with open(log_file_path, 'w', encoding='utf-8') as f:
        log_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        f.write(f"{log_time} 大模型：{first_question}\n")

    return jsonify({
        'next_question': first_question,
        'audio_url': f"/{audio}"
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
        'audio_url': f"/{audio}"  # 假设音频文件保存为静态文件，供前端播放
    })

@app.route('/start_text_interview', methods=['POST'])
def start_text_interview():
    global base_prompt, log_file_path  # 使用 global 关键字访问和修改全局变量
    if not base_prompt:
        base_prompt = "你作为面试官，请开始提问。"
    first_question = ask_one_question(base_prompt)
    # 生成日志文件名：面试+时间.txt
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"面试_{timestamp}.txt"
    log_file_path = os.path.join(app.config['INTERVIEW_LOG_FOLDER'], filename)

    # 记录第一条问题
    with open(log_file_path, 'w', encoding='utf-8') as f:
        log_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        f.write(f"{log_time} 大模型：{first_question}\n")
    # 返回第一个问题和音频URL
    return jsonify({
        'next_question': first_question,
    })

@app.route('/last_text_question', methods=['POST'])
def last_text_question():
    global base_prompt, log_file_path  # 使用 global 关键字访问和修改全局变量
    data = request.json
    input_list = data.get("text")
    base_prompt = f"{base_prompt}, 上一个问题用户的回答是{input_list}"
    last_question = ask_one_question(base_prompt)
    # 记录用户回答和新的问题
    log_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    if log_file_path:
        with open(log_file_path, 'a', encoding='utf-8') as f:
            f.write(f"{log_time} 用户：{input_list}\n")
            f.write(f"{log_time} 大模型：{last_question}\n")
    return jsonify({
        'next_question': last_question
    })


# 设置文件保存路径为 /input_audio
UPLOAD_FOLDER = 'input_audio'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


@app.route('/upload', methods=['POST'])
def upload_file():
    if 'audio' not in request.files:
        return jsonify({'error': 'No file part'}), 400

    audio_file = request.files['audio']

    if audio_file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    # 保存音频文件到 /input_audio 目录
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], 'recording.wav')
    audio_file.save(file_path)

    return jsonify({'message': 'File successfully uploaded', 'path': file_path})

@app.route('/upload_audio_final', methods=['POST'])
def upload_audio_final():
    global base_prompt, log_file_path

    if 'audio' not in request.files:
        return jsonify({'error': 'No file part'}), 400

    audio_file = request.files['audio']
    if audio_file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    file_path = os.path.join(app.config['UPLOAD_FOLDER'], 'recording.wav')
    audio_file.save(file_path)

    input_list = ai_speech.recognize_wav(file_path)
    base_prompt = f"{base_prompt}, 上一个问题用户的回答是{input_list}"
    last_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(last_question)

    # 记录用户回答和新的问题
    log_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    if log_file_path:
        with open(log_file_path, 'a', encoding='utf-8') as f:
            f.write(f"{log_time} 用户：{input_list}\n")
            f.write(f"{log_time} 大模型：{last_question}\n")

    return jsonify({
        'user_response': input_list,
        'next_question': last_question,
        'audio_url': f"/{audio}"
    })


# 提供音频文件访问
@app.route('/output_audio/<filename>')
def serve_audio(filename):
    return send_from_directory(app.config['OUTPUT_AUDIO_FOLDER'], filename)

conversation_history = []



if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
