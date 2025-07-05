package com.example.reply.ui.knowledgebase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@Composable
fun PersonalDocumentsScreen(userId: String) {
    // 添加协程作用域
    val scope = rememberCoroutineScope()

    var documents by remember { mutableStateOf(emptyList<Document>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var documentToDelete by remember { mutableStateOf<Document?>(null) }

    LaunchedEffect(userId) {
        try {
            val fileList = fetchDocuments(userId)
            documents = fileList.map {
                Document(
                    id = it.id,
                    title = it.filename,
                    type = getDocumentTypeFromFilename(it.filename),
                    date = parseIsoDate(it.created_at)
                )
            }
            isLoading = false
        } catch (e: Exception) {
            error = "加载失败: ${e.message}"
            isLoading = false
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && documentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除文档 \"${documentToDelete?.title}\" 吗？此操作不可恢复。") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        documentToDelete?.let { doc ->
                            // 使用 scope.launch 替代直接 launch
                            scope.launch {
                                try {
                                    deleteDocument(doc.id, userId)
                                    // 删除成功后更新列表
                                    documents = documents.filter { it.id != doc.id }
                                } catch (e: Exception) {
                                    error = "删除失败: ${e.message}"
                                }
                            }
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(error ?: "加载失败")
            }
        }
        documents.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text("暂无文档，请上传文件")
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(documents) { document ->
                    DocumentItem(
                        document = document,
                        onDeleteClick = {
                            documentToDelete = document
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

// 日期解析方法
private fun parseIsoDate(dateString: String): Date {
    return try {
        // 尝试解析带有时区的 ISO 8601 格式
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val localDateTime = LocalDateTime.parse(dateString, formatter)
        val calendar = Calendar.getInstance()
        calendar.set(
            localDateTime.year,
            localDateTime.monthValue - 1,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute,
            localDateTime.second
        )
        calendar.time
    } catch (e: DateTimeParseException) {
        try {
            // 尝试简化格式
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            formatter.parse(dateString) ?: Date()
        } catch (e2: Exception) {
            // 如果都失败，返回当前日期
            Date()
        }
    } catch (e: Exception) {
        Date()
    }
}

private suspend fun fetchDocuments(userId: String): List<FileResponse> {
    return withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.255.38:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(KnowledgeBaseApiService::class.java)
        val response = apiService.getFiles(userId)
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch documents: ${response.errorBody()?.string()}")
        }
    }
}

private suspend fun deleteDocument(fileId: Long, userId: String) {
    withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.255.38:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(KnowledgeBaseApiService::class.java)
        val response = apiService.deleteFile(DeleteRequest(fileId, userId))
        if (!response.isSuccessful) {
            throw Exception("Delete failed: ${response.errorBody()?.string()}")
        }
    }
}

// 更新文件类型识别逻辑 - 只处理指定的文件类型
private fun getDocumentTypeFromFilename(filename: String): DocumentType {
    return when (val ext = filename.substringAfterLast('.').lowercase()) {
        "pdf" -> DocumentType.PDF
        "txt" -> DocumentType.TXT
        "doc" -> DocumentType.DOC
        "docx" -> DocumentType.DOCX
        "pptx" -> DocumentType.PPTX
        else -> DocumentType.TXT // 其他类型视为文本文件
    }
}

@Composable
fun DocumentItem(
    document: Document,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F8)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文档类型图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getColorForType(document.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForType(document.type),
                    contentDescription = document.type.displayName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文档信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = document.type.displayName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(document.date),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )
            }

            // 删除按钮
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除文档",
                    tint = Color.Red
                )
            }
        }
    }
}

// 根据文档类型获取颜色 - 每种类型不同颜色
private fun getColorForType(type: DocumentType): Color {
    return when (type) {
        DocumentType.PDF -> Color(0xFFF44336) // 红
        DocumentType.TXT -> Color(0xFF2196F3) // 蓝
        DocumentType.DOC -> Color(0xFF3F51B5) // 深蓝
        DocumentType.DOCX -> Color(0xFF3F51B5) // 深蓝
        DocumentType.PPTX -> Color(0xFFFF5722) // 橙
    }
}

// 根据文档类型获取图标 - 每种类型不同图标
private fun getIconForType(type: DocumentType): ImageVector {
    return when (type) {
        DocumentType.PDF -> Icons.Default.PictureAsPdf
        DocumentType.TXT -> Icons.Default.Description
        DocumentType.DOC -> Icons.Default.Description // 使用文档图标
        DocumentType.DOCX -> Icons.Default.Description // 使用文档图标
        DocumentType.PPTX -> Icons.Default.Slideshow // 使用幻灯片图标
    }
}