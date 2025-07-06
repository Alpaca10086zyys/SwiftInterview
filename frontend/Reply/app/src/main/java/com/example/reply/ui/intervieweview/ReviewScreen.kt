package com.example.reply.ui.intervieweview

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.reply.ui.navigation.Route
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavController,userId: String) {
    var reviews by remember { mutableStateOf<List<InterviewReview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    isUploading = true
                    val success = uploadAudioFile(context, it, userId)
                    isUploading = false
                    if (success) {
                        navController.navigate(Route.DirectMessages) {
                            popUpTo(Route.DirectMessages) { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    isUploading = false
                    Toast.makeText(context, "上传出错: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("UploadCrash", "异常: ${e.stackTraceToString()}")
                }
            }
        }
    }

    if (isUploading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(enabled = false) {}, // 禁止交互
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }


    LaunchedEffect(Unit) {
        try {
            val data = fetchReviewData(userId)
            reviews = data.sortedByDescending { it.created_at }
        } catch (e: Exception) {
            Log.e("ReviewScreen", "网络请求失败: ${e.message}")
            errorMessage = "加载失败: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "面试复盘",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    launcher.launch("audio/*")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "上传")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    Text(text = errorMessage ?: "未知错误", color = Color.Red)
                }

                else -> {
                    LazyColumn {
                        items(reviews) { review ->
                            ReviewCard(review,navController)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ReviewCard(review: InterviewReview,navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val encodedTitle = Uri.encode(review.title)
                val encodedContent = Uri.encode(review.review_content ?: "无内容")
                navController.navigate("reviewDetail/$encodedTitle/$encodedContent")
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                review.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(6.dp))

            val created = formatDateTime(review.created_at)
            Text(
                text = "创建时间：$created",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "时长：${review.duration.h}小时 ${review.duration.m}分 ${review.duration.s}秒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            review.tags?.let { tags ->
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),) {
                    tags.focus?.forEach { TagChip(it) }
                    tags.job?.let { TagChip("岗位: $it") }
                    tags.style?.let { TagChip("风格: $it") }
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String) {
    AssistChip(
        onClick = {},
        modifier = Modifier
            .defaultMinSize(minHeight = 28.dp)
            .padding(end = 2.dp),
        label = {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
            )
                },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// 日期格式化函数：显示精确到时分
fun formatDateTime(isoString: String): String {
    return try {
        val dt = OffsetDateTime.parse(isoString)
        dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (e: Exception) {
        isoString
    }
}

// 使用 OkHttp 直接请求并用 Gson 解析
suspend fun fetchReviewData(userId: String): List<InterviewReview> {
    return withContext(Dispatchers.IO) {
        val url = "http://192.168.0.106:5000/api/review/list?user_id=$userId"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val json = response.body?.string()
                ?: throw Exception("响应为空")

            val type = object : TypeToken<List<InterviewReview>>() {}.type
            Gson().fromJson<List<InterviewReview>>(json, type)
        }
    }
}

suspend fun uploadAudioFile(context: Context, uri: Uri, userId: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = uri.path?.split("/")?.lastOrNull() ?: "recording.wav"

            val buffer = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext false

            val fileRequest = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, buffer.toRequestBody("audio/*".toMediaTypeOrNull()))
                .addFormDataPart("user_id", userId)
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("http://192.168.0.106:5000/api/review/upload_audio")
                .post(fileRequest)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val json = response.body?.string() ?: return@withContext false
                return@withContext json.contains("\"status\":\"accepted\"")
            }
        } catch (e: Exception) {
            Log.e("Upload", "上传失败: ${e.message}")
            return@withContext false
        }
    }
}


