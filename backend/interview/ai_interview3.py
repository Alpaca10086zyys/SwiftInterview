import uuid
import time
import requests
from auth_util import gen_sign_headers

# 请替换APP_ID、APP_KEY
APP_ID = '2025790177'  # 替换为你的APP_ID
APP_KEY = 'QyPmFFzeoZRKqrCO'  # 替换为你的APP_KEY
URI = '/vivogpt/completions'  # 同步接口
DOMAIN = 'api-ai.vivo.com.cn'
METHOD = 'POST'


# 提问并获取模型的回答
def ask_model(question_text, session_id, system_prompt, conversation_history=""):
    # 确保 prompt 是字符串类型
    if not isinstance(question_text, str):
        question_text = str(question_text)

    params = {
        'requestId': str(uuid.uuid4())  # 使用UUID生成唯一的请求ID
    }
    print('requestId:', params['requestId'])

    # 请求数据
    data = {
        'prompt': question_text,  # 模型将回答这个问题
        'sessionId': session_id,  # 会话ID
        'model': 'vivo-BlueLM-TB-Pro',  # 使用的模型
        'systemPrompt': system_prompt,  # 模型的角色设定（例如，面试官）
        'conversationHistory': conversation_history  # 将用户的回答和历史对话传递给模型
    }
    headers = gen_sign_headers(APP_ID, APP_KEY, METHOD, URI, params)  # 签名生成
    headers['Content-Type'] = 'application/json'  # 设置请求头

    start_time = time.time()
    url = 'http://{}{}'.format(DOMAIN, URI)  # 完整的URL地址
    response = requests.post(url, json=data, headers=headers, params=params)  # 同步请求

    if response.status_code == 200:  # 请求成功时
        res_obj = response.json()
        if res_obj['code'] == 0 and res_obj.get('data'):
            content = res_obj['data']['content']
            print(f"\n模型回答的内容：\n{content}")
            return content  # 返回模型的回答
        else:
            print(f"模型回答错误：{res_obj['msg']}")
            return "抱歉，出现错误，请再试。"
    else:  # 请求失败时
        print(response.status_code, response.text)
        return "抱歉，无法连接到模型服务器。"

    end_time = time.time()
    timecost = end_time - start_time
    print("请求耗时: %.2f秒" % timecost)


# 示例：调用ask_model提问
def ask_one_question(prompt):
    session_id = str(uuid.uuid4())  # 初始化会话ID
    system_prompt = "你是面试官，负责问问题。"  # 模型的角色定位
    question_text = prompt  # 问题内容

    # 获取模型回答
    model_response = ask_model(question_text, session_id, system_prompt)
    return model_response



if __name__ == "__main__":
    ask_one_question()
