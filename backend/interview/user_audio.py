import wave
import io
import os

from examples import TTS, AueType


class ShortTTS(object):
    vivoHelper = "vivoHelper"
    yunye = "yunye"
    wanqing = "wanqing"
    xiaofu = "xiaofu"
    yige_child = "yige_child"
    yige = "yige"
    yiyi = "yiyi"
    xiaoming = "xiaoming"


class LongTTS(object):
    x2_vivoHelper = "vivoHelper"
    x2_yige = "x2_yige"
    x2_yige_news = "x2_yige_news"
    x2_yunye = "x2_yunye"
    x2_yunye_news = "x2_yunye_news"
    x2_M02 = "x2_M02"
    x2_M05 = "x2_M05"
    x2_M10 = "x2_M10"
    x2_F163 = "x2_F163"
    x2_F25 = "x2_F25"
    x2_F22 = "x2_F22"
    x2_F82 = "x2_F82"


class Humanoid(object):
    F245_natural = "F245_natural"  # 知性柔美
    M24 = "M24"  # 俊朗男声
    M193 = "M193"  # 理性男声
    GAME_GIR_YG = "GAME_GIR_YG"  # 游戏少女
    GAME_GIR_MB = "GAME_GIR_MB"  # 游戏萌宝
    GAME_GIR_YJ = "GAME_GIR_YJ"  # 游戏御姐
    GAME_GIR_LTY = "GAME_GIR_LTY"  # 电台主播
    YIGEXIAOV = "YIGEXIAOV"  # 依格
    FY_CANTONESE = "FY_CANTONESE"  # 粤语
    FY_SICHUANHUA = "FY_SICHUANHUA"  # 四川话
    FY_MIAOYU = "FY_MIAOYU"  # 苗语


# 将 PCM 数据转换为 WAV 格式
def pcm2wav(pcmdata: bytes, channels=1, bits=16, sample_rate=24000):
    if bits % 8 != 0:
        raise ValueError("bits % 8 must == 0. now bits:" + str(bits))
    io_fd = io.BytesIO()
    wavfile = wave.open(io_fd, 'wb')
    wavfile.setnchannels(channels)
    wavfile.setsampwidth(bits // 8)
    wavfile.setframerate(sample_rate)
    wavfile.writeframes(pcmdata)
    wavfile.close()
    io_fd.seek(0)
    return io_fd


# 函数：传入文本并生成音频文件
def generate_audio_from_text(text, vcn='xiaofu', engineid='short_audio_synthesis_jovi'):
    input_params = {
        'app_id': '2025790177',
        'app_key': 'QyPmFFzeoZRKqrCO',
        'engineid': engineid
    }

    # 初始化 TTS 对象
    tts = TTS(**input_params)
    tts.open()

    # 生成音频数据
    pcm_buffer = tts.gen_radio(aue=AueType.PCM, vcn=vcn, text=text)

    if pcm_buffer:
        # 将 PCM 数据转换为 WAV 格式
        wav_io = pcm2wav(pcm_buffer)

        # 创建 output_audio 文件夹（如果不存在的话）
        output_dir = 'output_audio'
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)

        # 保存为 WAV 文件
        file_name = os.path.join(output_dir, f"{vcn}_audio.wav")
        with open(file_name, 'wb') as fd:
            fd.write(wav_io.read())

        print(f"Audio saved as {file_name}")
        return file_name  # 返回保存的文件名

    else:
        print("Failed to generate audio.")
        return None


if __name__ == '__main__':
    # 示例：调用函数生成音频
    text = "你好呀"
    generate_audio_from_text(text, vcn='xiaofu', engineid='short_audio_synthesis_jovi')
