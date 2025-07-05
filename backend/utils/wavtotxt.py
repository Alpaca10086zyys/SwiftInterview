# encoding: utf-8
import json
from utils.auth_util import gen_sign_headers

# 请替换APP_ID、APP_KEY
APP_ID = '2025790177'
APP_KEY = 'QyPmFFzeoZRKqrCO'
URI = '/vivogpt/completions/stream'
DOMAIN = 'api-ai.vivo.com.cn'
METHOD = 'POST'

import uuid, time, requests

# ------------------------------------------
# A. 格式化提示模板（推断说话人 + 重排文本）
# ------------------------------------------
FORMAT_TEMPLATE = """
你是一个优秀的语义理解助手。下面是一段自动语音转文字后的内容，没有任何标点或说话人区分。你的任务是：

1. **根据语言风格/语气/词汇内容**，自动识别每句是“面试官”（Interviewer）还是“候选人”（Candidate）说的。
2. 将对话拆分成句子，并加上说话人标签。
3. 保持原始意思不变，尽可能完整地复原对话。
4. 不添加任何说明或解释，只输出格式化后的结果。

输出格式示例如下：
```[01] Interviewer: 你好，请你先做个自我介绍。
[02] Candidate: 大家好，我叫张三，目前是大四学生。
[03] Interviewer: 你在校期间有没有参加过项目实践？
[04] Candidate: 有的，我参与了一个基于大数据的项目。
```

以下是原始转文字文本：
{text}
"""

# ------------------------------------------
# B. 新函数：让 VivoGPT 帮我们格式化
# ------------------------------------------
def format_transcript(raw_text: str) -> str:
    """
    调用 VivoGPT（非流式）把原始文本 => 两说话人已标注的列表
    """
    params = {'requestId': str(uuid.uuid4())}
    prompt = FORMAT_TEMPLATE.format(text=raw_text.strip())
    data = {
        "prompt": prompt,
        "sessionId": str(uuid.uuid4()),
        "model": "vivo-BlueLM-TB-Pro"   # 用小模型就够；可替换
    }

    headers = gen_sign_headers(APP_ID, APP_KEY, METHOD, '/vivogpt/completions', params)
    headers["Content-Type"] = "application/json"

    url = f"http://{DOMAIN}/vivogpt/completions"
    resp = requests.post(url, json=data, headers=headers, params=params, timeout=30)
    resp.raise_for_status()                # 若非 200 会直接抛异常

    result = resp.json()
    print("格式化结果:", result)
    return result.get("data").get("content", "").strip()