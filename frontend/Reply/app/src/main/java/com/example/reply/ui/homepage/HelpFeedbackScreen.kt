package com.example.reply.ui.homepage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelpFeedbackScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("帮助与反馈", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        HelpAndFeedbackScreen()
    }
}

@Composable
fun HelpAndFeedbackScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("关于方便面", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        FAQItem(
            question = "我们是谁？",
            answer = "我们是『方便面』，一个智能模拟面试应用，致力于通过AI技术帮助用户提升面试表现。"
        )
        Spacer(modifier = Modifier.height(12.dp))

        FAQItem(
            question = "我们能干什么？",
            answer = "我们运用AI模拟真实面试场景，为你提供个性化问题、反馈、面试技巧建议，让你更从容地面对正式面试。"
        )
        Spacer(modifier = Modifier.height(12.dp))

        FAQItem(
            question = "首页的五个功能按钮分别是什么？",
            answer = """
                1. 模拟面试：进入AI模拟面试环节。
                2. 面试复盘：查看历史面试记录和系统反馈。
                3. 知识库：查阅各类面试相关知识。
                4. 每日一题：每日一道精选面试题，持续练习。
                5. 个人主页：查看账户信息、进度与设置。
            """.trimIndent()
        )
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column {
        Text(text = "Q: $question", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "A: $answer", style = MaterialTheme.typography.bodyMedium)
    }
}
