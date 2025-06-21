package com.example.reply.ui.interview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.layout.*

@Composable
fun EntryPage(onStartClicked: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onStartClicked,) {
            Text("开始面试")
        }
    }
}
