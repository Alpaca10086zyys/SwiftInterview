package com.example.reply.ui.interview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*

enum class InterviewPage {
    Entry,
    CustomInterview,
    //QuestionPage // 后续扩展
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
                //currentPage = InterviewPage.QuestionPage // 后续跳转
            }
        )
        //InterviewPage.QuestionPage -> QuestionPage() // TODO
    }
}
