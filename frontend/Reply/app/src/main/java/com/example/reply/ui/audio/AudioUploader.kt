package com.example.reply.ui.audio
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

object AudioUploader {
    fun uploadAudio(file: File, serverUrl: String, onResult: (Boolean, String?) -> Unit) {
        val client = OkHttpClient()

        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name,
                file.asRequestBody("audio/mp4".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                onResult(response.isSuccessful, response.body?.string())
            }
        })
    }
}
