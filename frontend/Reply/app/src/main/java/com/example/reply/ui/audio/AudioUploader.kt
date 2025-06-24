package com.example.reply.ui.audio
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object AudioUploader {
    fun uploadAudio(
        file: File,
        serverUrl: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        Thread {
            try {
                // val client = OkHttpClient()
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val fileBody = file.asRequestBody("audio/wav".toMediaType())
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("audio", file.name, fileBody)
                    .build()

                val request = Request.Builder()
                    .url(serverUrl)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val json = JSONObject(body ?: "{}")
                    val audioUrl = json.getString("audio_url")
                    onResult(true, audioUrl)
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, null)
            }
        }.start()
    }
}

