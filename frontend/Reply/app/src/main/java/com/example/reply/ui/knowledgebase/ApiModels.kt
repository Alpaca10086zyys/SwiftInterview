package com.example.reply.ui.knowledgebase

import java.util.Date

// 文件列表响应模型
data class FileResponse(
    val id: Long,
    val user_id: String,
    val filename: String,
    val filepath: String,
    val created_at: String
)

// 上传响应模型
data class UploadResponse(
    val message: String,
    val file_id: Long,
    val filename: String,
    val user_id: String
)

// 删除请求模型
data class DeleteRequest(
    val file_id: Long,
    val user_id: String
)

// 删除响应模型
data class DeleteResponse(
    val message: String,
    val file_id: Long,
    val user_id: String
)

// 文档类型枚举
enum class DocumentType(
    val displayName: String
) {
    PDF("PDF文档"),
    TXT("文本文件"),
    JPG("图片文件"),
    MP4("视频文件")
}

// 文档数据模型
data class Document(
    val id: Long,
    val title: String,
    val type: DocumentType,
    val date: Date
)