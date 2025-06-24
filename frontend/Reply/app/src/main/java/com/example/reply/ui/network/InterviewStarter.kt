package com.example.reply.network

import android.media.MediaPlayer
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object InterviewStarter {
    fun startInterview(
        serverUrl: String,
        onResult: (success: Boolean, question: String?, audioUrl: String?) -> Unit
    ) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(serverUrl)
            .post(okhttp3.RequestBody.create(null, ByteArray(0))) // 空POST
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        Log.d("InterviewStarter", "响应：$body")
                        val json = JSONObject(body ?: "")
                        val question = json.getString("next_question")
                        val audioUrl = json.getString("audio_url")
                        onResult(true, question, audioUrl)
                    } else {
                        onResult(false, null, null)
                    }
                }
            } catch (e: Exception) {
                onResult(false, null, null)
                e.printStackTrace()
            }
        }.start()
    }

    fun playAudio(audioUrl: String) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e("InterviewStarter", "播放音频失败: $e")
        }
    }
}
