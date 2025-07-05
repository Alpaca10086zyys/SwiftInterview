# encoding: utf-8
import json
import uuid
import time
import requests
from utils.auth_util import gen_sign_headers

# 请替换APP_ID、APP_KEY
APP_ID = '2025790177'
APP_KEY = 'QyPmFFzeoZRKqrCO'
URI = '/vivogpt/completions/stream'
DOMAIN = 'api-ai.vivo.com.cn'
METHOD = 'POST'

import uuid, time, requests
from typing import List

# ------------------------------------------
# 1. 帮助函数：把对话记录拼成 Prompt
# ------------------------------------------
EVAL_TEMPLATE = """
你是一名经验丰富的技术面试主管，请根据以下面试对话，\
从【技术深度】【沟通表达】【问题解决】【思维方式】【职业动机】【抗压表现】\
六个角度，对候选人做客观评价，并给出每个角度 1‑10 的评分及改进建议。\
最后给出 Overall Score（1‑100）与一句总结性评语。\
要求输出 Markdown 格式，结构如下：

### 1. 技术深度
- 评分: x/10
- 评语: ...

### 2. 沟通表达
...

### Overall Score
- 85/100  
- 简短总结: ...

下面是完整对话记录（已按时间排序）：
{transcript}
"""


def build_prompt(transcript_text: str) -> str:
    """把原始对话文本插入到模板里"""
    return EVAL_TEMPLATE.format(transcript=transcript_text.strip())


# ------------------------------------------
# 2. 主函数：流式调用 BlueLM 并实时打印
# ------------------------------------------
def stream_vivogpt_eval(transcript_text: str, output_path="output.md", ws=None):
    params = {'requestId': str(uuid.uuid4())}
    print('requestId:', params['requestId'])

    prompt = build_prompt(transcript_text)
    data = {
        "prompt": prompt,
        "sessionId": str(uuid.uuid4()),
        "model": "vivo-BlueLM-TB-Pro"
    }

    headers = gen_sign_headers(APP_ID, APP_KEY, METHOD, URI, params)
    headers["Content-Type"] = "application/json"

    start_time = time.time()
    url = f"http://{DOMAIN}{URI}"

    try:
        with requests.post(url,
                           json=data,
                           headers=headers,
                           params=params,
                           stream=True,
                           timeout=(5, 60)) as resp:

            if resp.status_code != 200:
                print(resp.status_code, resp.text)
                return

            first_line = True
            with open(output_path, 'w', encoding='utf-8') as fout:
                for raw in resp.iter_lines():
                    if not raw:
                        continue
                    if first_line:
                        first_line = False
                        print(f"首字节耗时: {time.time() - start_time:.2f}s")

                    line = raw.decode("utf-8", errors="ignore")
                    if line.startswith("data:"):
                        line = line[5:].lstrip()

                    if not line.strip():
                        continue

                    try:
                        data = json.loads(line)
                        text = data.get("message", "")
                    except json.JSONDecodeError:
                        print("[JSON Decode Error]", line)
                        continue

                    # 写入本地文件
                    fout.write(text)
                    fout.flush()

                    # 推送到 WebSocket 客户端
                    if ws is not None:
                        try:
                            ws.send(line)
                        except Exception as e:
                            print("[WebSocket Error]", e)

                    # 打印到控制台
                    print(line, flush=True)

    except requests.exceptions.RequestException as e:
        print("请求失败:", e)

    print(f"请求总耗时: {time.time() - start_time:.2f}s")

# ------------------------------------------
# 3. 使用示例
# ------------------------------------------
if __name__ == "__main__":
    transcript = """
    [09:01:15] Interviewer: 早上好，我们开始面试吧。请先做个自我介绍。
    [09:01:22] Candidate: 大家好，我叫张三，目前就读于某大学软件工程……
    [09:05:48] Interviewer: 说说你最近一个项目中遇到的最大技术难点？
    [09:06:10] Candidate: 我们遇到的问题主要是大规模并发写入……
    ...
    """
    stream_vivogpt_eval(transcript)
