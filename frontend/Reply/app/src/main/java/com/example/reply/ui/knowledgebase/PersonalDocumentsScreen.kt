package com.example.reply.ui.knowledgebase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PersonalDocumentsScreen() {
    val documents = remember {
        listOf(
            Document(
                id = 1,
                title = "项目需求文档",
                type = DocumentType.PDF,
                date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2)
            ),
            Document(
                id = 2,
                title = "会议记录",
                type = DocumentType.TXT,
                date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5)
            ),
            Document(
                id = 3,
                title = "设计草图",
                type = DocumentType.JPG,
                date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 10)
            ),
            Document(
                id = 4,
                title = "产品演示视频",
                type = DocumentType.MP4,
                date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 30)
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(documents) { document ->
            DocumentItem(document = document)
        }
    }
}

data class Document(
    val id: Long,
    val title: String,
    val type: DocumentType,
    val date: Date
)

enum class DocumentType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    PDF("PDF", Icons.Default.PictureAsPdf, Color(0xFFF44336)),
    TXT("文本", Icons.Default.Description, Color(0xFF2196F3)),
    JPG("图片", Icons.Default.PictureAsPdf, Color(0xFF4CAF50)),
    MP4("视频", Icons.Default.VideoFile, Color(0xFFFF9800))
}

@Composable
fun DocumentItem(document: Document) {
    val dateFormat = remember { SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F8)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文档类型图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(document.type.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = document.type.icon,
                    contentDescription = document.type.displayName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文档信息
            Column {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = document.type.displayName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(document.date),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )
            }
        }
    }
}