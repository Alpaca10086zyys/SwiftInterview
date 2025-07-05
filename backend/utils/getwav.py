import pyttsx3
from pydub import AudioSegment
import os

# 1. 用 pyttsx3 生成临时音频（默认是 wav 或平台相关格式，Windows 下通常是 wav）
engine = pyttsx3.init()
engine.save_to_file("你好先做个简单的自我介绍吧我叫李明是一名计算机专业的本科毕业生在校期间我主要学习了数据结构操作系统计算机网络等核心课程最近的一次项目经验是我和团队一起开发了一个基于Vue和Flask的图书管理系统主要负责前端模块开发能详细讲一下你在这个项目中遇到的技术难点吗当时我们在做借阅记录分页的时候遇到了性能瓶颈后来通过引入虚拟滚动优化了加载速度你为什么选择前端方向作为你的求职方向因为我在实习期间接触到了前端开发发现自己对页面交互和用户体验特别感兴趣也擅长解决样式和逻辑结合的问题你对未来的职业规划是怎样的我希望能先深入掌握VueReact等主流框架未来往全栈方向发展有没有了解过我们的公司产品我了解你们的主要产品是企业协作平台我也试用过觉得界面非常简洁你还有什么想问我们的吗我想了解一下贵公司的前端团队是如何进行协作的以及对新人有没有一套成长机制", 'temp.wav')
engine.runAndWait()

# 2. 如需标准化 WAV（可选，根据需求调整参数）
sound = AudioSegment.from_wav('temp.wav')
# 可添加处理：如调整采样率、声道等，这里直接导出标准 WAV
sound.export('output.wav', format='wav')

# 3. 删除临时文件（可选）
os.remove('temp.wav')