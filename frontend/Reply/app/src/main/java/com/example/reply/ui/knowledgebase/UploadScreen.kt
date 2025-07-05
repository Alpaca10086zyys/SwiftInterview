package com.example.reply.ui.knowledgebase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

enum class UploadState {
    IDLE, UPLOADING, SUCCESS, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(navController: NavController, userId: String) {
    // 检查用户ID是否有效
    if (userId.isEmpty()) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var selectedFile by remember { mutableStateOf<File?>(null) }
    var uploadState by remember { mutableStateOf(UploadState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri == null) {
                errorMessage = "未选择文件"
                return@rememberLauncherForActivityResult
            }

            try {
                val fileName = getFileNameFromUri(context, uri)
                val inputStream = context.contentResolver.openInputStream(uri)

                // 确保文件名安全
                val safeFileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                val file = File(context.cacheDir, safeFileName)

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedFile = file
                errorMessage = null // 清除之前的错误
            } catch (e: Exception) {
                errorMessage = "文件选择失败: ${e.message}"
                Log.e("UploadScreen", "File selection error", e)
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("上传文件") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = "上传",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = selectedFile?.name ?: "点击上传你的文件",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 更新支持的文件类型提示
            Text(
                text = selectedFile?.let {
                    val sizeKB = it.length() / 1024
                    val sizeMB = sizeKB / 1024.0
                    if (sizeMB > 1) "%.2f MB".format(sizeMB) else "$sizeKB KB"
                } ?: "支持文件类型: txt, pdf, doc, docx, pptx",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uploadState != UploadState.UPLOADING
            ) {
                Text("选择文件")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedFile?.let { file ->
                        coroutineScope.launch {
                            uploadState = UploadState.UPLOADING
                            errorMessage = null // 清除之前的错误

                            try {
                                // 检查文件大小
                                if (file.length() > 10 * 1024 * 1024) { // 10MB限制
                                    errorMessage = "文件大小超过10MB限制"
                                    uploadState = UploadState.ERROR
                                    return@launch
                                }

                                Log.d("UploadScreen", "开始上传文件: ${file.name}, 大小: ${file.length()} 字节")

                                val response = uploadFile(file, userId)

                                if (response.isSuccessful) {
                                    uploadState = UploadState.SUCCESS
                                    Log.d("UploadScreen", "上传成功: ${response.body()?.message}")
                                    // 短暂延迟后返回
                                    kotlinx.coroutines.delay(1500)
                                    navController.popBackStack()
                                } else {
                                    val errorBody = response.errorBody()?.string() ?: "未知错误"
                                    errorMessage = "上传失败: ${response.code()} - $errorBody"
                                    uploadState = UploadState.ERROR
                                    Log.e("UploadScreen", "上传失败: $errorBody")
                                }
                            } catch (e: Exception) {
                                errorMessage = "上传异常: ${e.message}"
                                uploadState = UploadState.ERROR
                                Log.e("UploadScreen", "上传错误", e)
                            }
                        }
                    } ?: run {
                        errorMessage = "请先选择文件"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedFile != null && uploadState != UploadState.UPLOADING
            ) {
                when (uploadState) {
                    UploadState.IDLE -> Text("上传文件")
                    UploadState.UPLOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    UploadState.SUCCESS -> Text("上传成功")
                    UploadState.ERROR -> Text("上传失败，重试")
                }
            }

            if (uploadState == UploadState.ERROR && errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "上传失败，请重试",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private suspend fun uploadFile(file: File, userId: String): Response<UploadResponse> {
    return withContext(Dispatchers.IO) {
        try {
            val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            // 使用正确的表单字段名称
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.255.26:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(KnowledgeBaseApiService::class.java)
            apiService.uploadFile(part, userIdBody)
        } catch (e: Exception) {
            Log.e("UploadScreen", "上传异常", e)
            // 创建错误响应
            Response.error(500, okhttp3.ResponseBody.create(null, e.message ?: "Unknown error"))
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1 && cut != null) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown_file_${System.currentTimeMillis()}"
}