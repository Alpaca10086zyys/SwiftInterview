package com.example.reply.ui.knowledgebase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.reply.ui.navigation.ReplyNavigationActions

@Composable
fun KnowledgeBaseScreen(navController: NavController, userId: String) {
    var selectedTab by remember { mutableStateOf(KnowledgeBaseTab.PERSONAL_DOCS) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 搜索框和按钮行
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 搜索框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    textStyle = TextStyle(fontSize = 14.sp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    },
                    placeholder = { Text("搜索", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )

                // "去搜索"按钮
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // 传递用户ID和查询内容
                        navController.navigate("searchResult/${userId}/${searchText.text}")
                    },
                    modifier = Modifier
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp), // 圆角
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D47A1), // 深蓝色
                        contentColor = Color.White // 白色文字
                    )
                ) {
                    Text("去搜索")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 标签选择区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KnowledgeBaseTab.values().forEach { tab ->
                    TabButton(
                        text = tab.title,
                        isSelected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 内容区域
            when (selectedTab) {
                KnowledgeBaseTab.PERSONAL_DOCS -> PersonalDocumentsScreen(userId = userId)
                KnowledgeBaseTab.INTERVIEW_ARCHIVE -> InterviewArchiveScreen()
            }
        }

        // 悬浮按钮
        FloatingActionButton(
            onClick = {
                ReplyNavigationActions(navController as NavHostController).navigateToUpload(userId)
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "上传",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp)), // 移除 weight
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

enum class KnowledgeBaseTab(val title: String) {
    PERSONAL_DOCS("个人文档"),
    INTERVIEW_ARCHIVE("面试归档")
}