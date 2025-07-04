package com.example.reply.ui.knowledgebase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.random.Random

// 搜索结果数据类
data class SearchResult(
    val id: Int,
    val title: String,
    val content: String,
    val similarity: Float // 相似度 (0-1)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(navController: NavController, query: String?) {
    var searchText by remember { mutableStateOf(query ?: "") }
    var searchResults by remember { mutableStateOf(generateSearchResults(searchText)) }

    // 执行搜索的函数
    fun performSearch() {
        searchResults = generateSearchResults(searchText)
    }

    // 初始化时执行一次搜索
    LaunchedEffect(Unit) {
        performSearch()
    }

    Scaffold(
        topBar = {
            // 添加顶部内边距
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回知识库",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        // 搜索框和按钮行 - 添加内边距
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
                                placeholder = { Text("搜索个人文档", fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                )
                            )

                            // 搜索按钮 - 点击执行搜索
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { performSearch() },
                                modifier = Modifier
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0D47A1)
                                )
                            ) {
                                Text("搜索")
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // 添加水平内边距和顶部间距
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 增加搜索框与卡片列表之间的间距 (24dp > 卡片间16dp)
            Spacer(modifier = Modifier.height(24.dp))

            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults) { result ->
                        SearchResultCard(result)
                    }
                }
            } else {
                // 没有搜索结果时的提示
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有找到相关文档",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

// 生成搜索结果 (模拟) - 添加长文本测试
fun generateSearchResults(query: String): List<SearchResult> {
    if (query.isEmpty()) return emptyList()

    val longText = "这是一个非常长的文档内容，用于测试卡片内部的滚动功能。" +
            "当文本内容超过卡片固定高度时，用户应该能够在卡片内部滚动查看完整内容。" +
            "这是为了确保用户体验良好，即使文档内容非常长也能方便阅读。" +
            "长文本测试：".repeat(20) +
            "文档结束。"

    val mockDocuments = listOf(
        "项目需求文档" to "这是关于项目需求的详细文档，包含功能描述和技术要求...",
        "会议记录" to "本次会议讨论了项目进度和下一步计划，重点解决了技术难题...",
        "设计草图" to "产品UI设计初稿，包含主要界面布局和交互流程...",
        "技术方案" to "系统架构设计和技术选型方案，详细说明了各模块的实现方式...",
        "用户反馈" to "收集到的用户反馈汇总，包含功能建议和问题报告...",
        "长文档测试" to longText  // 添加长文本测试
    )

    return mockDocuments.mapIndexed { index, (title, content) ->
        val similarity = 0.7f + Random.nextFloat() * 0.3f
        SearchResult(index, title, content, similarity)
    }
}

// 搜索结果卡片组件 - 减小高度并添加内部滚动
@Composable
fun SearchResultCard(result: SearchResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp), // 减小卡片高度到180dp
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // 浅蓝色背景
        )
    ) {
        // 卡片内部可滚动区域
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // 添加垂直滚动
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                // 相似度标签
                Text(
                    text = "${(result.similarity * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容预览 - 可滚动
            Text(
                text = result.content,
                color = Color.Black,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}