package com.example.reply.network

import android.os.Handler
import android.os.Looper
import com.example.reply.data.UserData
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

object ApiService {
    private const val BASE_URL = "http://192.168.255.10:5000/api/user"
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun login(
        email: String,
        password: String,
        callback: (success: Boolean, user: UserData?, message: String) -> Unit
    ) {
        val url = "$BASE_URL/login".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("email", email)
            ?.addQueryParameter("password", password)
            ?.build()

        println("登录请求URL: ${url?.toString()}")

        val request = Request.Builder()
            .url(url!!)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("登录网络错误: ${e.message}")
                mainHandler.post {
                    callback(false, null, "网络错误: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                println("登录响应: ${response.code} - $responseBody")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (response.isSuccessful) {
                        val userId = jsonResponse.optString("id", "")
                        val nickname = jsonResponse.optString("nickname")
                            .takeIf { it.isNotEmpty() }
                            ?: jsonResponse.optString("user_name")
                                .takeIf { it.isNotEmpty() }
                            ?: jsonResponse.optString("name", "用户")

                        // 新增字段
                        val createdAt = jsonResponse.optString("created_at", "")
                        val jobStatus = jsonResponse.optString("job_status", "离校-随时到岗")

                        if (userId.isNotEmpty()) {
                            val user = UserData(
                                id = userId,
                                email = email,
                                nickname = nickname,
                                createdAt = createdAt,
                                jobStatus = jobStatus
                            )
                            mainHandler.post {
                                callback(true, user, "登录成功")
                            }
                        } else {
                            mainHandler.post {
                                callback(false, null, "用户信息不完整")
                            }
                        }
                    } else {
                        val errorMsg = jsonResponse.optString("message", "登录失败: ${response.code}")
                        mainHandler.post {
                            callback(false, null, errorMsg)
                        }
                    }
                } catch (e: Exception) {
                    println("登录解析错误: ${e.message}")
                    mainHandler.post {
                        callback(false, null, "解析错误: ${e.message}")
                    }
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

        println("注册请求JSON: $json")

        val request = Request.Builder()
            .url("$BASE_URL/add_user")
            .post(RequestBody.create(JSON_MEDIA_TYPE, json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("注册网络错误: ${e.message}")
                mainHandler.post {
                    callback(false, "网络错误: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                println("注册响应: ${response.code} - $responseBody")

                try {
                    if (response.isSuccessful) {
                        mainHandler.post {
                            callback(true, "注册成功")
                        }
                    } else {
                        val jsonResponse = JSONObject(responseBody)
                        val errorMsg = jsonResponse.optString("error", "注册失败: ${response.code}")
                        mainHandler.post {
                            callback(false, errorMsg)
                        }
                    }
                } catch (e: Exception) {
                    println("注册解析错误: ${e.message}")
                    mainHandler.post {
                        callback(false, "解析错误: ${e.message}")
                    }
                }
            }
        })
    }

    fun updateJobStatus(
        userId: String,
        newStatus: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("job_status", newStatus)
        }

        val request = Request.Builder()
            .url("$BASE_URL/update_job_status/$userId")
            .patch(RequestBody.create(JSON_MEDIA_TYPE, json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("更新求职状态网络错误: ${e.message}")
                mainHandler.post {
                    callback(false, "网络错误: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                println("更新求职状态响应: ${response.code} - $responseBody")

                try {
                    if (response.isSuccessful) {
                        mainHandler.post {
                            callback(true, "求职状态更新成功")
                        }
                    } else {
                        val jsonResponse = JSONObject(responseBody)
                        val errorMsg = jsonResponse.optString("error", "更新失败: ${response.code}")
                        mainHandler.post {
                            callback(false, errorMsg)
                        }
                    }
                } catch (e: Exception) {
                    println("更新求职状态解析错误: ${e.message}")
                    mainHandler.post {
                        callback(false, "解析错误: ${e.message}")
                    }
                }
            }
        })
    }
}