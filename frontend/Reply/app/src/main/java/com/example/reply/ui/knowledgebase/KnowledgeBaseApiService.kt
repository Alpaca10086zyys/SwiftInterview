package com.example.reply.ui.knowledgebase

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface KnowledgeBaseApiService {
    // 获取文件列表
    @GET("api/knowledge/list")
    suspend fun getFiles(@Query("user_id") userId: String): Response<List<FileResponse>>

    // 上传文件
    @Multipart
    @POST("api/knowledge/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): Response<UploadResponse>

    // 删除文件 - 修复版本
    @POST("api/knowledge/delete")
    suspend fun deleteFile(
        @Body body: DeleteRequest
    ): Response<DeleteResponse>
}