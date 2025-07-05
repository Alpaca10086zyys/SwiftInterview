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

// 文档类型枚举 - 只保留需要的文件类型
enum class DocumentType(
    val displayName: String
) {
    PDF("PDF文档"),
    TXT("文本文件"),
    DOC("Word文档"),
    DOCX("Word文档"),
    PPTX("PPT文档")
}

// 文档数据模型
data class Document(
    val id: Long,
    val title: String,
    val type: DocumentType,
    val date: Date
)

// 搜索结果模型
data class SearchResultResponse(
    val file_id: Long,
    val filename: String,
    val content: String,
    val similarity: Float
)

// 搜索请求模型
data class SearchRequest(
    val query: String,
    val user_id: String,
    val threshold: Float = 0.7f,
    val top_k: Int = 5
)

// 搜索响应模型
data class SearchResponse(
    val results: List<SearchResultResponse>,
    val query: String,
    val user_id: String
)