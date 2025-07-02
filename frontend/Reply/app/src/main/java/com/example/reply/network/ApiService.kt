package com.example.reply.network

import com.example.reply.data.UserData
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

object ApiService {
    private const val BASE_URL = "http://192.168.255.42:5000/api/user"
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun login(
        email: String,
        password: String,
        callback: (success: Boolean, user: UserData?, message: String) -> Unit
    ) {
        val url = "$BASE_URL/login".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("email", email)
            ?.addQueryParameter("password", password)
            ?.build()

        val request = Request.Builder()
            .url(url!!)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null, "网络错误: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (response.isSuccessful) {
                        // 根据后端实际返回的字段名调整
                        val userId = jsonResponse.optString("id", "")
                        val nickname = jsonResponse.optString("user_name", "用户")

                        if (userId.isNotEmpty()) {
                            val user = UserData(
                                id = userId,
                                email = email,
                                nickname = nickname
                            )
                            callback(true, user, "登录成功")
                        } else {
                            callback(false, null, "用户信息不完整")
                        }
                    } else {
                        val errorMsg = jsonResponse.optString("message", "登录失败: ${response.code}")
                        callback(false, null, errorMsg)
                    }
                } catch (e: Exception) {
                    callback(false, null, "解析错误: ${e.message}")
                }
            }
        })
    }

    fun register(
        email: String,
        password: String,
        nickname: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("user_name", nickname)
        }

        val request = Request.Builder()
            .url("$BASE_URL/add_user")
            .post(RequestBody.create(JSON_MEDIA_TYPE, json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "网络错误: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    if (response.isSuccessful) {
                        callback(true, "注册成功")
                    } else {
                        val jsonResponse = JSONObject(responseBody)
                        val errorMsg = jsonResponse.optString("error", "注册失败: ${response.code}")
                        callback(false, errorMsg)
                    }
                } catch (e: Exception) {
                    callback(false, "解析错误: ${e.message}")
                }
            }
        })
    }
}