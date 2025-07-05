package com.example.reply.ui.interview

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.reply.R
import com.example.reply.network.InterviewStarter
import com.example.reply.ui.audio.AudioUploader
import com.example.reply.ui.audio.WavRecorder
import java.io.File

@Composable
fun QuestionPage(onExitInterview: () -> Unit) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var interviewStarted by remember { mutableStateOf(false) }
    val recorder = remember { WavRecorder(context) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isRobotSpeaking by remember { mutableStateOf(false) }

    // 动画相关：机器人 alpha 和 scale 动画（只在 speaking 时触发）
    val alpha by animateFloatAsState(
        targetValue = if (isRobotSpeaking) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robotAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isRobotSpeaking) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robotScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val (imageRef, buttonRef) = createRefs()

            // 机器人动画
            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = "Robot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(
                        alpha = alpha,
                        scaleX = scale,
                        scaleY = scale
                    )
                    .constrainAs(imageRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(buttonRef.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )


            // 👇 通用按钮组，取代原有两个 AnimatedVisibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(buttonRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                val buttonColors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording)
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isRecording)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var buttonPressed by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            if (!interviewStarted) {
                                isPlaying = true
                                InterviewStarter.startInterview(
                                    serverUrl = "http://192.168.0.106:5000/api/interview/start_interview"
                                ) { success, _, audioUrl ->
                                    if (success && audioUrl != null) {
                                        val fullUrl = "http://192.168.0.106:5000/api/interview/$audioUrl"
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(fullUrl)
                                            prepare()
                                            setOnCompletionListener {
                                                isPlaying = false
                                                isRobotSpeaking = false
                                                interviewStarted = true
                                            }
                                            isRobotSpeaking = true
                                            start()
                                        }
                                    } else {
                                        isPlaying = false
                                        isRobotSpeaking = false
                                    }
                                }
                            } else if (!isRecording) {
                                recorder.startRecording()
                                isRecording = true
                            } else {
                                isRecording = false
                                isPlaying = true
                                val file = recorder.stopRecording()
                                recordedFile = file
                                Log.d("WavRecorder", "保存路径: ${file?.absolutePath}")
                                file?.let {
                                    AudioUploader.uploadAudio(
                                        it,
                                        serverUrl = "http://192.168.0.106:5000/api/interview/upload_audio_final"
                                    ) { success, newAudioUrl ->
                                        if (success && newAudioUrl != null) {
                                            val fullUrl = "http://192.168.0.106:5000/api/interview/$newAudioUrl"
                                            try {
                                                mediaPlayer?.release()
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(fullUrl)
                                                    prepare()
                                                    setOnCompletionListener {
                                                        isPlaying = false
                                                        isRobotSpeaking = false
                                                    }
                                                    isRobotSpeaking = true
                                                    start()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                isPlaying = false
                                                isRobotSpeaking = false
                                            }
                                        } else {
                                            isPlaying = false
                                            isRobotSpeaking = false
                                        }
                                    }
                                } ?: run {
                                    isPlaying = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .scale(if (buttonPressed) 0.96f else 1f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        buttonPressed = true
                                        tryAwaitRelease()
                                        buttonPressed = false
                                    }
                                )
                            },
                        colors = buttonColors,
                        enabled = !isPlaying
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isPlaying) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(end = 8.dp)
                                        .offset(y = 3.dp)
                                )
                            }

                            Text(
                                when {
                                    !interviewStarted && isPlaying -> "对话中..."
                                    !interviewStarted -> "开始面试"
                                    isRecording -> "停止回答"
                                    isPlaying -> "回答中..."
                                    else -> "开始回答"
                                }
                            )
                        }

                    }

                    var showDialog by remember { mutableStateOf(false) }

                    // 关闭按钮
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    // 弹出对话框确认是否退出
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = {
                                Text(text = "退出面试")
                            },
                            text = {
                                Text("你确定要退出当前面试吗？")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDialog = false
                                        onExitInterview()  // 返回首页
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("确定")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showDialog = false
                                    }
                                ) {
                                    Text("取消")
                                }
                            }
                        )
                    }
                }
            }
        }

    }
}
