package com.example.reply.ui.homepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reply.data.UserData

@Composable
fun GroupsMainPage(
    onLoginClicked: () -> Unit,
    userData: UserData? = null,
    onLogout: () -> Unit = {}
) {
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

            // 数据统计区域
            StatsSection()

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