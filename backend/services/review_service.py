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
# EVAL_TEMPLATE = """
# 你是一名经验丰富的技术面试主管，请根据以下面试对话，\
# 从【技术深度】【沟通表达】【问题解决】【思维方式】【职业动机】【抗压表现】\
# 六个角度，对候选人做客观评价，并给出每个角度 1‑10 的评分及改进建议。\
# 最后给出 Overall Score（1‑100）与一句总结性评语。\
# 要求输出 Markdown 格式，结构如下：
#
# ### 1. 技术深度
# - 评分: x/10
# - 评语: ...
#
# ### 2. 沟通表达
# ...
#
# ### Overall Score
# - 85/100
# - 简短总结: ...
#
# 下面是完整对话记录（已按时间排序）：
# {transcript}
# """
EVAL_TEMPLATE = """
你是一位专业的智能面试分析助手，负责为技术面试过程生成详尽的复盘报告。\
请根据以下原始面试对话内容，从以下六个维度进行智能分析，总结候选人的表现，\
并提供清晰、专业且具有可读性的评价报告。

---

### 📊 分析维度：

1. 💻 技术深度：基础是否扎实？是否具备实际项目经验？能否解释关键技术点？
2. 🗣️ 沟通表达：表达是否清晰？术语使用是否准确？是否能准确理解问题？
3. 🧠 问题解决：面对挑战是否能分解问题？思路是否合理、具备可执行性？
4. 🧩 思维方式：是否体现结构化、逻辑推理、抽象建模能力？
5. 🚀 职业动机：是否展现明确目标？是否愿意学习？与岗位是否匹配？
6. 🔥 抗压表现：面对追问、难题时是否冷静、自信？是否主动应对？

---

请针对每个维度，输出以下结构内容（使用 Markdown）：

### ✅ <维度名称>
- **评分**: x / 10
- **评语**: （2-4 行）请具体描述该维度表现，可指出亮点和不足。
- **建议**: （1-2 行）提出可行的提升方向或关注点。

---

在六个维度分析之后，请生成：

### 🧾 综合评分与建议
- **Overall Score**: xx / 100
- **总结评语**: 用 1~2 句话综合评估候选人表现，是否推荐、是否具有潜力等。
- **适岗建议**: 简要分析其与目标岗位的匹配度、培养潜力或风险点。

---

请确保整体语气专业客观、语言表达清晰自然、结构排版整齐，适合在企业招聘系统中归档或作为反馈发送。

以下是原始对话内容（请勿在输出中重复）：
{transcript}
"""



def build_prompt(transcript_text: str) -> str:
    """把原始对话文本插入到模板里"""
    return EVAL_TEMPLATE.format(transcript=transcript_text.strip())


# ------------------------------------------
# 2. 主函数：流式调用 BlueLM 并实时打印
# ------------------------------------------
def stream_vivogpt_eval(transcript_text: str, ws=None) -> str:
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

    result_text = ""  # 👈 用于累积全部生成内容

    try:
        with requests.post(url,
                           json=data,
                           headers=headers,
                           params=params,
                           stream=True,
                           timeout=(5, 60)) as resp:

            if resp.status_code != 200:
                print(resp.status_code, resp.text)
                return ""

            first_line = True
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

                # 累积到结果中
                result_text += text

                # 推送给 WebSocket 客户端（如果有）
                if ws is not None:
                    try:
                        ws.send(text)
                    except Exception as e:
                        print("[WebSocket Error]", e)

                # 打印实时内容
                print(text, flush=True)

    except requests.exceptions.RequestException as e:
        print("请求失败:", e)
        return ""

    print(f"请求总耗时: {time.time() - start_time:.2f}s")
    return result_text  # 👈 最后返回完整的评估 Markdown 文本

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


# ------------------------------------------
# C. 小改动：在主流程里先格式化再评估
# ------------------------------------------
def run_pipeline(raw_text):
    formatted = format_transcript(raw_text)
    print("📑 已格式化文本：\n", formatted, "\n---")

    return stream_vivogpt_eval(formatted)


# ------------------------------------------
# 3. 使用示例
# ------------------------------------------
if __name__ == "__main__":
    # transcript = """
    # [09:01:15] Interviewer: 早上好，我们开始面试吧。请先做个自我介绍。
    # [09:01:22] Candidate: 大家好，我叫张三，目前就读于某大学软件工程……
    # [09:05:48] Interviewer: 说说你最近一个项目中遇到的最大技术难点？
    # [09:06:10] Candidate: 我们遇到的问题主要是大规模并发写入……
    # ...
    # """
    # stream_vivogpt_eval(transcript)

    raw_text = """
    你好先做个简单的自我介绍吧我叫李明是一名计算机专业的本科毕业生在校期间我主要学习了数据结构操作系统计算机网络等核心课程最近的一次项目经验是我和团队一起开发了一个基于Vue和Flask的图书管理系统主要负责前端模块开发能详细讲一下你在这个项目中遇到的技术难点吗当时我们在做借阅记录分页的时候遇到了性能瓶颈后来通过引入虚拟滚动优化了加载速度你为什么选择前端方向作为你的求职方向因为我在实习期间接触到了前端开发发现自己对页面交互和用户体验特别感兴趣也擅长解决样式和逻辑结合的问题你对未来的职业规划是怎样的我希望能先深入掌握VueReact等主流框架未来往全栈方向发展有没有了解过我们的公司产品我了解你们的主要产品是企业协作平台我也试用过觉得界面非常简洁你还有什么想问我们的吗我想了解一下贵公司的前端团队是如何进行协作的以及对新人有没有一套成长机制
    """
    print("---\n开始格式化并评估对话记录...")
    run_pipeline(raw_text)
