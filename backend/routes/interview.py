from flask import Blueprint, request, jsonify, send_from_directory
from interview_audio.ai_interview3 import ask_one_question
from interview_audio.user_audio import generate_audio_from_text
from interview_audio.asr import ai_speech
from interview_audio.prompt import get_depth_description, get_jumpiness_description
import os
from datetime import datetime

interview_bp = Blueprint('interview', __name__, url_prefix='/api/interview')

# 全局状态变量
base_prompt = ""
log_file_path = ""
UPLOAD_FOLDER = 'input_audio'
OUTPUT_AUDIO_FOLDER = 'output_audio'
INTERVIEW_LOG_FOLDER = 'txt'

os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_AUDIO_FOLDER, exist_ok=True)
os.makedirs(INTERVIEW_LOG_FOLDER, exist_ok=True)


@interview_bp.route('/modify-text', methods=['POST'])
def modify_text():
    global base_prompt
    data = request.json
    job_title = data.get("job_title")
    style = data.get("style")
    imp = data.get("imp")
    base_prompt = f"这是一场针对{job_title}岗位的面试，你作为面试官，你的风格是{style},你的关注重点是{imp},请你开始问问题"
    return base_prompt

@interview_bp.route('/modify-text_new', methods=['POST'])
def modify_text_new():
    global base_prompt
    data = request.json
    job_title = data.get("job_title")
    style = data.get("style")
    imp = data.get("imp")
    jumpiness = get_jumpiness_description(data.get("jumpiness_level"))
    depth = get_depth_description(data.get("depth"))
    base_prompt = f"这是一场针对{job_title}岗位的面试，你作为面试官，你的风格是{style},你的关注重点是{imp},思维跳跃程度是{jumpiness},问题深度是{depth},请你开始问问题，只能询问一个问题"
    return base_prompt

@interview_bp.route('/start_interview', methods=['POST'])
def start_interview():
    global base_prompt, log_file_path
    first_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(first_question)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    log_file_path = os.path.join(INTERVIEW_LOG_FOLDER, f"面试_{timestamp}.txt")
    with open(log_file_path, 'w', encoding='utf-8') as f:
        f.write(f"{datetime.now():%Y-%m-%d %H:%M:%S} 大模型：{first_question}\n")
    return jsonify({'next_question': first_question, 'audio_url': f"/{audio}"})


@interview_bp.route('/upload_audio', methods=['POST'])
def upload_audio():
    global base_prompt
    file = request.files['file']
    input_list = ai_speech.recognize_wav(file)
    base_prompt += f"，上一个问题用户的回答是{input_list}"
    last_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(last_question)
    return jsonify({'user_response': input_list, 'next_question': last_question, 'audio_url': f"/{audio}"})


@interview_bp.route('/start_text_interview', methods=['POST'])
def start_text_interview():
    global base_prompt  # 使用 global 关键字访问和修改全局变量
    if not base_prompt:
        base_prompt = "你作为面试官，请开始提问。"
    first_question = ask_one_question(base_prompt)

    # 返回第一个问题和音频URL
    return jsonify({
        'next_question': first_question,
    })

@interview_bp.route('/last_text_question', methods=['POST'])
def last_text_question():
    global base_prompt  # 使用 global 关键字访问和修改全局变量
    data = request.json
    input_list = data.get("text")
    base_prompt = f"{base_prompt}, 上一个问题用户的回答是{input_list}"
    last_question = ask_one_question(base_prompt)
    return jsonify({
        'next_question': last_question
    })
@interview_bp.route('/upload', methods=['POST'])
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

@interview_bp.route('/upload_audio_final', methods=['POST'])
def upload_audio_final():
    global base_prompt  # 使用 global 关键字访问和修改全局变量
    if 'audio' not in request.files:
        return jsonify({'error': 'No file part'}), 400

    audio_file = request.files['audio']

    if audio_file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    # 保存音频文件到 /input_audio 目录
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], 'recording.wav')
    audio_file.save(file_path)
    input_list = ai_speech.recognize_wav(file_path)
    base_prompt = f"{base_prompt}, 上一个问题用户的回答是{input_list}"
    last_question = ask_one_question(base_prompt)
    audio = generate_audio_from_text(last_question)
    return jsonify({
        'user_response':input_list,
        'next_question': last_question,
        'audio_url': f"/{audio}"  # 假设音频文件保存为静态文件，供前端播放
    })



# 静态资源访问示例
@interview_bp.route('/output_audio/<filename>')
def serve_audio(filename):
    return send_from_directory(OUTPUT_AUDIO_FOLDER, filename)
