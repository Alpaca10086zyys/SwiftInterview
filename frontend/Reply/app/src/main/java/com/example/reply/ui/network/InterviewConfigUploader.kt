package com.example.reply.ui.network
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object InterviewConfigUploader {
    fun uploadConfig(
        job: String,
        style: String,
        focus: List<String>,
        serverUrl: String,
        onResult: (success: Boolean, response: String?) -> Unit
    ) {
        val json = JSONObject().apply {
            put("job_title", job)
            put("style", style)
            put("imp", focus.joinToString("、"))
        }

        val client = OkHttpClient()
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        onResult(true, response.body?.string())
                    } else {
                        onResult(false, response.body?.string())
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }.start()
    }
}
