package com.example.reply.ui.interview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import com.example.reply.ui.EmptyComingSoon

enum class InterviewPage {
    Entry,
    CustomInterview,
    ChatInterview// 后续扩展
}

@Composable
fun InterviewScreen() {
    var currentPage by remember { mutableStateOf(InterviewPage.Entry) }

    when (currentPage) {
        InterviewPage.Entry -> EntryPage(onStartClicked = {
            currentPage = InterviewPage.CustomInterview
        })

        InterviewPage.CustomInterview -> CustomInterviewPage(
            onStartInterview = {
                currentPage = InterviewPage.ChatInterview // 后续跳转
            }
        )

        InterviewPage.ChatInterview -> QuestionPage()
    }
}
