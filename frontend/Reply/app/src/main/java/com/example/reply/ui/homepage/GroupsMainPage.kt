package com.example.reply.ui.homepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reply.data.UserData
import com.example.reply.network.ApiService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun GroupsMainPage(
    onLoginClicked: () -> Unit,
    userData: UserData? = null,
    onLogout: () -> Unit = {},
    onJobStatusUpdated: (String) -> Unit = {}  // 可传给 ViewModel 做持久化
) {
    // 求职状态
    val jobStatusInitial = userData?.jobStatus ?: "离校-随时到岗"
    var jobStatus by remember { mutableStateOf(jobStatusInitial) }

    // 使用天数
    val daysUsed = remember(userData) {
        userData?.createdAt?.let { createdAt ->
            calculateDaysUsed(createdAt)
        } ?: 0
    }

    // 处理求职状态更新
    val updateJobStatus: (String) -> Unit = { newStatus ->
        jobStatus = newStatus
        userData?.id?.let { userId ->
            ApiService.updateJobStatus(userId, newStatus) { success, message ->
                if (!success) {
                    // 失败时恢复原状态
                    jobStatus = jobStatusInitial
                    // 显示错误消息（实际应用中可用 Snackbar）
                    println("求职状态更新失败: $message")
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户信息区域
            UserInfoSection(
                onLoginClick = onLoginClicked,
                userData = userData,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 数据统计区域（使用天数 & 求职状态）
            StatsSection(
                daysUsed = daysUsed,
                jobStatus = jobStatus,
                onJobStatusChange = updateJobStatus
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 功能按钮区域
            FunctionButtonsSection()

            Spacer(modifier = Modifier.height(24.dp))

            // 雷达图区域
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    RadarChart(
                        values = listOf(4.2f, 3.8f, 4.5f, 3.5f, 4.0f, 4.7f),
                        labels = listOf("结构化表达", "项目经验", "行业理解", "即兴应变", "专业深度", "沟通能力"),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(300.dp)
                    )
                }
            }
        }
    }
}

private fun calculateDaysUsed(createdAt: String): Int {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val createdDate = LocalDateTime.parse(createdAt, formatter)
        val now = LocalDateTime.now()
        ChronoUnit.DAYS.between(createdDate, now).toInt()
    } catch (e: Exception) {
        0
    }
}