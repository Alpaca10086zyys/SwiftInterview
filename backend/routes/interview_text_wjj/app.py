from flask import Flask, request, jsonify,send_from_directory
from interview_answer import answer_question
from interview_question import ask_one_question
import os

app = Flask(__name__)


@app.route('/')
def index():
    return app.send_static_file('index_text.html')  # 保证 index.html 是静态文件夹里的



@app.route('/start_interview', methods=['POST'])
def start_interview():
    question = ask_one_question()
    answer = answer_question(question)

    # 返回第一个问题和音频URL
    return jsonify({
        'question': question,
        'answer': answer
    })




if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
