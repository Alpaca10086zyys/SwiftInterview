# -*- coding: utf-8 -*-
import wave
import io


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


'''
input:
    pcmdata: pcm audio data
output:
    wav file-like object
'''


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


if __name__ == '__main__':
    from examples import TTS, AueType

    for k, v in ShortTTS.__dict__.items():
        if k.find('__') != -1:
            continue
        print(k, v)
        input_params = {
            # 修改为你的app_id 和 app_key
            'app_id': '2025790177',
            'app_key': 'QyPmFFzeoZRKqrCO',
            'engineid': 'short_audio_synthesis_jovi'
        }
        tts = TTS(**input_params)
        tts.open()
        # pcm
        pcm_buffer = tts.gen_radio(aue=AueType.PCM, vcn=k, text='你好呀')
        wav_io = pcm2wav(pcm_buffer)
        with open(f'{k}_pcm.wav', 'wb') as fd:
            fd.write(wav_io.read())
        break

    for k, v in LongTTS.__dict__.items():
        if k.find('__') != -1:
            continue
        print(k, v)
        input_params = {
            # 修改为你的app_id 和 app_key
            'app_id': '2025790177',
            'app_key': 'QyPmFFzeoZRKqrCO',
            'engineid': 'long_audio_synthesis_screen'
        }
        tts = TTS(**input_params)
        tts.open()
        # pcm
        pcm_buffer = tts.gen_radio(aue=AueType.PCM, vcn=k, text='你好呀')
        wav_io = pcm2wav(pcm_buffer)
        with open(f'{k}_pcm.wav', 'wb') as fd:
            fd.write(wav_io.read())
        break

    for k, v in Humanoid.__dict__.items():
        if k.find('__') != -1:
            continue
        print(k, v)
        input_params = {
            # 修改为你的app_id 和 app_key
            'app_id': '2025790177',
            'app_key': 'QyPmFFzeoZRKqrCO',
            'engineid': 'tts_humanoid_lam'
        }
        tts = TTS(**input_params)
        tts.open()
        # pcm
        pcm_buffer = tts.gen_radio(aue=AueType.PCM, vcn=k, text='你好呀')
        wav_io = pcm2wav(pcm_buffer)
        with open(f'{k}_pcm.wav', 'wb') as fd:
            fd.write(wav_io.read())
        break