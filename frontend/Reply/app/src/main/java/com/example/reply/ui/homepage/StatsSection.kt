@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.reply.ui.homepage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSection(
    daysUsed: Int,
    jobStatus: String,
    onJobStatusChange: (String) -> Unit,
    interviewCount: Int = 12
) {
    val jobStatusOptions = listOf(
        "离校-随时到岗",
        "在校-月内到岗",
        "在校-考虑机会",
        "在校-暂不考虑",
        "离职-随时到岗",
        "在职-月内到岗",
        "在职-暂不考虑"
    )

    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //标记在上，数值在下
            StatItem(title = "模拟面试次数", value = interviewCount.toString())

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )

            StatItem(title = "使用天数", value = daysUsed.toString())

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )

            // 求职状态选择器
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .wrapContentSize()
            ) {
                Text(
                    text = "求职状态",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    // 下拉触发区域
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expanded = true }
                    ) {
                        Text(
                            text = jobStatus,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "展开求职状态选项",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f) // 限制宽度，避免过宽
                    ) {
                        jobStatusOptions.forEach { status ->
                            DropdownMenuItem(
                                onClick = {
                                    onJobStatusChange(status)
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 标题在上，数值在下
@Composable
fun StatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}