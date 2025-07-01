from flask import Blueprint, request, jsonify
from interview_text_wjj.interview_answer import answer_question
from interview_text_wjj.interview_question import ask_one_question

interview_text_bp = Blueprint('interview_text', __name__, url_prefix='/api/text')

@interview_text_bp.route('/start_interview', methods=['POST'])
def que_ans():
    question = ask_one_question()
    answer = answer_question(question)
    return jsonify({
        'question': question,
        'answer': answer
    })
