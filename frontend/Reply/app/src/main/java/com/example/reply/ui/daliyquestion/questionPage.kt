package com.example.reply.ui.daliyquestion

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.reply.R
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuestionPage() {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var answerText by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf("请解释Compose的工作原理？") }
    var referenceAnswer by remember { mutableStateOf<String?>("未提供参考答案") }
    var userSubmittedAnswer by remember { mutableStateOf<String?>(null) }
    var showAnswerButton by remember { mutableStateOf(false) }
    var showAnswerCards by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }


    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    if (showDatePicker) {
        ShowMaterial3DatePicker(
            initialDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }

    // 每次日期变动时自动加载题目
    LaunchedEffect(selectedDate) {
        fetchQuestionAndAnswerForDate(selectedDate) { q, a ->
            questionText = q
            referenceAnswer = a
            userSubmittedAnswer = null
            showAnswerButton = false
            showAnswerCards = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showAnswerCards) {
                                showAnswerCards = false
                                showAnswerButton = false
                                answerText = userSubmittedAnswer ?: ""
                            }
                        },
                        enabled = showAnswerCards
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = if (showAnswerCards) MaterialTheme.colorScheme.onSurface else Color.Transparent
                        )
                    }
                },
                title = {
                    Text(
                        text = "每日一题",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // 保留可添加 action 的位置
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )


        },
        bottomBar = {
            if (!showAnswerCards) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入你的答案...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            // 处理答案发送逻辑
                            userSubmittedAnswer = answerText
                            showAnswerButton = true
                            answerText = ""
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "发送")
                    }
                }
            }
        }
    ) { innerPadding ->
        // 日期
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = selectedDate.format(dateFormatter),
                modifier = Modifier
                    .clickable { showDatePicker = true }
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 问题文字（更大字体）
            if (showAnswerCards && referenceAnswer != null && userSubmittedAnswer != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("参考答案", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(referenceAnswer!!)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("用户回答", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(userSubmittedAnswer!!)
                    }
                }
            } else {
            Text(
                text = questionText,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 图片展示（保持比例，不填充）
            Image(
                painter = painterResource(id = R.drawable.sample_question_image),
                contentDescription = "灯泡",
                modifier = Modifier
                    .width(280.dp)
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )

            if (showAnswerButton && userSubmittedAnswer != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showAnswerCards = true }) {
                    Text("查看答案")
                }
            }
        }

        }
    }
}

fun fetchQuestionAndAnswerForDate(date: LocalDate, onResult: (String, String) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://<你的IP>:5000/daily_question?date=${date.toString()}") // 假设GET请求
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            val json = JSONObject(response.body?.string() ?: "{}")
            val question = json.optString("question", "加载失败")
            val answer = json.optString("answer", "暂无参考答案")
            onResult(question, answer)
        }
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowMaterial3DatePicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
