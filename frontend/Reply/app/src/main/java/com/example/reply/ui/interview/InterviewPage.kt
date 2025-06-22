package com.example.reply.ui.interview

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.reply.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.reply.ui.audio.AudioUploader
import com.example.reply.ui.audio.WavRecorder
import java.io.File

@Composable
fun QuestionPage() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    val recorder = remember { WavRecorder(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        ConstraintLayout(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val (imageRef, buttonRef) = createRefs()

            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = "Robot",
                modifier = Modifier
                    .size(200.dp)
                    .constrainAs(imageRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(buttonRef.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

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
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(buttonRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 主按钮：开始 / 停止回答
                Button(
                    onClick = {
                        if (!isRecording) {
                            recorder.startRecording()
                        } else {
                            val file = recorder.stopRecording()
                            Log.d("WavRecorder", "保存路径: ${file?.absolutePath}")
//                        val mediaPlayer = MediaPlayer()
//                        mediaPlayer.setDataSource(context, Uri.fromFile(recordedFile))
//                        mediaPlayer.prepare()
//                        mediaPlayer.start()

//                        file?.let {
//                            AudioUploader.uploadAudio(
//                                it,
//                                serverUrl = "http://<真实IP>:5000/upload"
//                            ) { success, message ->
//                                Log.d("Upload", "Success=$success, Message=$message")
//                            }
//                        }
                        }
                        isRecording = !isRecording
                    },
                    colors = buttonColors,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(if (isRecording) "停止回答" else "开始回答")
                }

                // 右侧圆形关闭按钮
                IconButton(
                    onClick = { /* 关闭操作 */ },
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
            }
        }
    }
}


