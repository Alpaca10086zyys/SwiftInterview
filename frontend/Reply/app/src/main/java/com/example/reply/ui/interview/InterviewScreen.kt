package com.example.reply.ui.interview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import com.example.reply.ui.EmptyComingSoon

enum class InterviewPage {
    Entry,
    CustomInterview,
    ChatInterview,
    VoiceInterview
}

@Composable
fun InterviewScreen(userId: String) {
    var currentPage by remember { mutableStateOf(InterviewPage.Entry) }

    when (currentPage) {
        InterviewPage.Entry -> EntryPage(onStartClicked = {
            currentPage = InterviewPage.CustomInterview
        })

        InterviewPage.CustomInterview -> CustomInterviewPage(
            userId = userId,
            onStartInterview = { mode ->
                currentPage = if (mode == "文字面试") {
                    InterviewPage.ChatInterview
                } else {
                    InterviewPage.VoiceInterview
                }
            }
        )

//        InterviewPage.ChatInterview -> QuestionPage(
//            onExitInterview = { currentPage = InterviewPage.Entry }
//        )

        InterviewPage.ChatInterview ->InterviewChatPage()

        InterviewPage.VoiceInterview -> QuestionPage(
            onExitInterview = { currentPage = InterviewPage.Entry }
        )
    }
}

