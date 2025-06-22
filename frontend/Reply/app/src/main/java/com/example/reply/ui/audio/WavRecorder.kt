package com.example.reply.ui.audio
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private lateinit var outputFile: File

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    fun startRecording(): File {
        outputFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "interview_${System.currentTimeMillis()}.wav"
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        isRecording = true
        audioRecord?.startRecording()

        val outputStream = FileOutputStream(outputFile)
        writeWavHeader(outputStream, sampleRate, channelConfig, audioFormat)

        recordingThread = Thread {
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                Log.d("WavRecorder", "读取了 $read 字节")
                if (read > 0) {
                    Log.d("AudioSample", buffer.take(10).joinToString())
                    outputStream.write(buffer, 0, read)
                }
            }

            // 填写 WAV 文件头（补全长度）
            updateWavHeader(outputFile)
            outputStream.close()
        }
        recordingThread?.start()

        return outputFile
    }

    fun stopRecording(): File? {
        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        // 等待写入线程执行完毕（补全 WAV header）
        recordingThread?.join()
        recordingThread = null

        return outputFile
    }


    private fun writeWavHeader(out: OutputStream, sampleRate: Int, channel: Int, encoding: Int) {
        val channels = if (channel == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val bitsPerSample = if (encoding == AudioFormat.ENCODING_PCM_16BIT) 16 else 8

        val byteRate = sampleRate * channels * bitsPerSample / 8

        val header = ByteArray(44)

        // ChunkID
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        // ChunkSize (临时填0，稍后更新)
        // Format
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // Subchunk1ID
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        // Subchunk1Size = 16
        header[16] = 16
        // AudioFormat = 1 (PCM)
        header[20] = 1
        // NumChannels
        header[22] = channels.toByte()
        // SampleRate
        ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate)
        // ByteRate
        ByteBuffer.wrap(header, 28, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(byteRate)
        // BlockAlign
        header[32] = (channels * bitsPerSample / 8).toByte()
        // BitsPerSample
        header[34] = bitsPerSample.toByte()
        // Subchunk2ID
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        // Subchunk2Size (临时填0)

        out.write(header, 0, 44)
    }

    private fun updateWavHeader(wavFile: File) {
        val sizes = wavFile.length() - 44
        val file = RandomAccessFile(wavFile, "rw")
        file.seek(4)
        file.writeInt(Integer.reverseBytes((sizes + 36).toInt()))
        file.seek(40)
        file.writeInt(Integer.reverseBytes(sizes.toInt()))
        file.close()
    }
}
