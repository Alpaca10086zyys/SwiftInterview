import soundfile
import gevent
from urllib import parse
from gevent import monkey
import struct
import json
import sys
import time
from websocket import create_connection
from auth_util import gen_sign_headers
monkey.patch_all()
NUM = 1


def read_wave_data(wav_path):
    wav_data, sample_rate = soundfile.read(wav_path, dtype='int16')
    return wav_data, sample_rate


def send_process(ws, wav_path):
    try:
        for i in range(NUM):
            wav_data, sample_rate = read_wave_data(wav_path)

            nlen = len(wav_data)
            nframes = nlen * 2
            pack_data = struct.pack('%dh' % nlen, *wav_data)
            wav_data_c = list(struct.unpack('B' * nframes, pack_data))

            cur_frames = 0
            sample_frames = 1280

            start_data = {
                "type": "started",
                "request_id": "req_id",
                "asr_info": {
                    "front_vad_time": 6000,
                    "end_vad_time": 2000,
                    "audio_type": "pcm",
                    "chinese2digital": 1,
                    "punctuation": 2,
                },
                "business_info": "{\"scenes_pkg\":\"com.tencent.qqlive\", \"editor_type\":\"3\", \"pro_id\":\"2addc42b7ae689dfdf1c63e220df52a2-2020\"}"
            }

            start_data_json_str = json.dumps(start_data)
            ws.send(start_data_json_str)

            while cur_frames < nframes:
                samp_remaining = nframes-cur_frames
                num_samp = sample_frames if sample_frames < samp_remaining else samp_remaining

                list_tmp = [None] * num_samp

                for j in range(num_samp):
                    list_tmp[j] = wav_data_c[cur_frames + j]

                pack_data_2 = struct.pack('%dB' % num_samp, *list_tmp)
                cur_frames += num_samp

                if len(pack_data_2) < 1280:
                    break

                ws.send_binary(pack_data_2)

                time.sleep(0.04)

            enddata = b'--end--'
            ws.send_binary(enddata)

        closedata = b'--close--'
        ws.send_binary(closedata)
    except Exception as e:
        print(e)
        return

result_list = []

def recv_process(ws, tbegin, wav_path):
    index = 1
    cnt = 1
    first_world = 1
    first_world_time = 0

    while True:
        try:
            r = ws.recv()
            print(r)
            tmpobj = json.loads(r)

            # 如果是识别结果
            if tmpobj["action"] == "result" and tmpobj["type"] == "asr":
                text = tmpobj["data"]["text"]  # 提取识别的文字
                if text:  # 如果文字不为空
                    print("实时结果：", text)

                # 如果是最终结果
                if tmpobj["data"]["is_last"]:
                    result_list.append(text)  # 收集最终结果
                    print("最终结果已记录：", text)

                # 首词时间记录
                if first_world == 1 and text != "":
                    tfirst = int(round(time.time() * 1000))
                    first_world = 0
                    first_world_time = tfirst - tbegin

                # 打印最终结果信息
                if tmpobj["data"]["is_last"]:
                    r = json.dumps(tmpobj)
                    tend = int(round(time.time() * 1000))
                    path = wav_path
                    sid = tmpobj["sid"]
                    rid = tmpobj.get("request_id", "NULL")
                    code = tmpobj["code"]
                    t1 = first_world_time
                    t2 = tend - tbegin
                    t3 = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                    print("{}#{}#{}#{}#{}#{}#{}#{}".format(path, text, rid, sid, code, t1, t2, t3))
                    first_world = 1
                    tbegin = int(round(time.time() * 1000))
                    cnt = cnt + 1
                    if cnt > NUM:
                        return

            # 如果是错误或 VAD 信息
            elif tmpobj["action"] == "error" or tmpobj["action"] == "vad":
                r = json.dumps(tmpobj)
                print("{}".format(r.encode('utf-8').decode('unicode_escape')))
                path = wav_path
                sid = tmpobj["sid"]
                code = tmpobj["code"]
                t3 = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                print("{} {} {} {}".format(path, sid, code, t3))

        except Exception as e:
            print(e)
            path = wav_path
            err = "exception"
            t3 = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
            print("{} {} {}".format(path, err, t3))
            return


def control_process(wav_path):
    t = int(round(time.time() * 1000))

    params = {'client_version': parse.quote('unknown'), 'product': parse.quote('x'), 'package': parse.quote('unknown'),
              'sdk_version': parse.quote('unknown'), 'user_id': parse.quote('2addc42b7ae689dfdf1c63e220df52a2'),
              'android_version': parse.quote('unknown'), 'system_time': parse.quote(str(t)), 'net_type': 1,
              'engineid': "shortasrinput"}

    appid = '2025790177'
    appkey = 'QyPmFFzeoZRKqrCO'
    uri = '/asr/v2'
    domain = 'api-ai.vivo.com.cn'

    headers = gen_sign_headers(appid, appkey, 'GET', uri, params)

    param_str = ''
    seq = ''

    for key, value in params.items():
        value = str(value)
        param_str = param_str + seq + key + '=' + value
        seq = '&'

    ws = create_connection('ws://' + domain + '/asr/v2?' + param_str, header=headers)
    co1 = gevent.spawn(send_process, ws, wav_path)
    co2 = gevent.spawn(recv_process, ws, t, wav_path)
    gevent.joinall([co1, co2])
    time.sleep(0.04)


def main():
    if len(sys.argv) < 2:
        print('usage :  python %s conf' % sys.argv[0])
        print('example: python %s %s' % (sys.argv[0], 'audio.conf'))
        sys.exit(1)
    else:
        config = sys.argv[1]

    with open(config, 'rt') as f:
        line = f.readline().strip()

    coro = []
    t = gevent.spawn(control_process, line)
    coro.append(t)
    gevent.joinall(coro)
    print(result_list)


if __name__ == "__main__":
    main()
