package com.example.reply.ui.knowledgebase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InterviewArchiveScreen() {
    // 提供的面试归档时间戳数据
    val archiveTimestamps = listOf(
        "2025-07-01 12:54:42.516352+00",
        "2025-07-05 18:50:55.862648+00",
        "2025-07-05 20:09:56.590717+00",
        "2025-07-05 20:20:04.62699+00",
        "2025-07-06 02:37:56.333068+00"
    )

    // 将时间戳转换为格式化日期
    val archiveItems = remember(archiveTimestamps) {
        archiveTimestamps.map { timestamp ->
            val parsedDate = parseArchiveTimestamp(timestamp)
            val formattedDate = formatArchiveDate(parsedDate)
            ArchiveItem(
                id = UUID.randomUUID().toString(),
                title = "${formattedDate}面试归档.txt",
                displayDate = formatDisplayDate(parsedDate)
            )
        }
    }

    // 状态管理已删除的项目
    var archives by remember { mutableStateOf(archiveItems) }

    if (archives.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无面试归档内容",
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(archives, key = { it.id }) { item ->
                ArchiveCardItem(
                    item = item,
                    onDelete = {
                        archives = archives.filter { it.id != item.id }
                    }
                )
            }
        }
    }
}

// 面试归档项数据类
data class ArchiveItem(
    val id: String,
    val title: String,
    val displayDate: String
)

// 更健壮的时间戳解析方法
private fun parseArchiveTimestamp(timestamp: String): Date {
    return try {
        // 移除小数秒部分和时区信息，只保留到秒
        val simplifiedTimestamp = timestamp.substringBefore(".") + "Z"
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        formatter.parse(simplifiedTimestamp) ?: Date()
    } catch (e: Exception) {
        // 如果解析失败，使用当前时间
        Date()
    }
}

// 格式化归档日期 (用于文件名)
private fun formatArchiveDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return formatter.format(date)
}

// 格式化显示日期 (用于卡片底部)
private fun formatDisplayDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Composable
fun ArchiveCardItem(
    item: ArchiveItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F8) // 浅灰色背景
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左边文档图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)), // 蓝色背景
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "文本文档",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文档信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "文档类型: txt",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.displayDate,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )
            }

            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除归档",
                    tint = Color.Red
                )
            }
        }
    }
}