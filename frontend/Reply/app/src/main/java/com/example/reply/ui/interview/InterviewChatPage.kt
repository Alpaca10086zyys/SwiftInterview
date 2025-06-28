package com.example.reply.ui.interview

import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.xr.compose.testing.toDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewChatPage() {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    var userInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // Pair<内容, 是否是用户>

    // 启动时调用接口获取第一个问题
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.0.100:5000/start_text_interview")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connect()

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val question = json.getString("next_question")
                chatHistory.add(question to false)
            } catch (e: Exception) {
                Log.e("ChatInit", "Error: ${e.localizedMessage}")
            }
        }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize(),

        topBar = {
            TopAppBar(
                title = { Text(
                    "AI 面试",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },

        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("请输入回答...") }
                    )
                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                chatHistory.add(userInput to true)
                                val inputToSend = userInput
                                userInput = ""

                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val url = URL("http://192.168.0.100:5000/last_text_question")
                                        val connection = url.openConnection() as HttpURLConnection
                                        connection.requestMethod = "POST"
                                        connection.doOutput = true
                                        connection.setRequestProperty("Content-Type", "application/json")
                                        connection.setRequestProperty("Accept", "application/json")
                                        connection.connect()

                                        val json = JSONObject().apply {
                                            put("text", inputToSend)
                                        }

                                        connection.outputStream.use { os ->
                                            OutputStreamWriter(os, Charsets.UTF_8).use { writer ->
                                                writer.write(json.toString())
                                                writer.flush()
                                            }
                                        }

                                        val responseCode = connection.responseCode
                                        if (responseCode == 200) {
                                            val response = connection.inputStream.bufferedReader().readText()
                                            val nextQuestion = JSONObject(response).getString("next_question")
                                            withContext(Dispatchers.Main) {
                                                chatHistory.add(nextQuestion to false)
                                                scrollState.animateScrollToItem(chatHistory.size - 1)
                                            }
                                        } else {
                                            val errorText = connection.errorStream?.bufferedReader()?.readText()
                                            Log.e("ChatError", "Response Code: $responseCode, Error: $errorText")
                                        }

                                        connection.disconnect()
                                    } catch (e: Exception) {
                                        Log.e("ChatError", "Error occurred", e)
                                    }
                                }
                            }
                        }
                    ) {
                        Text("发送")
                    }
                }
            }

        }
    ) { innerPadding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(chatHistory.size) { index ->
                val (message, isUser) = chatHistory[index]

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300, delayMillis = 50)),
                    exit = fadeOut()
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isUser)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .widthIn(0.dp, 280.dp)
                        ) {
                            Text(
                                message,
                                color = if (isUser)
                                    MaterialTheme.colorScheme.onSecondary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
