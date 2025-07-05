package com.example.reply.ui.knowledgebase

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(navController: NavController, userId: String, query: String?) {
    var searchText by remember { mutableStateOf(query ?: "") }
    var searchResults by remember { mutableStateOf(emptyList<SearchResultResponse>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 执行搜索的函数
    fun performSearch() {
        if (searchText.isEmpty()) return

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("http://192.168.255.26:5000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(KnowledgeBaseApiService::class.java)
                    apiService.searchFiles(
                        SearchRequest(
                            query = searchText,
                            user_id = userId
                        )
                    )
                }

                if (response.isSuccessful) {
                    // 从响应中提取results数组
                    searchResults = response.body()?.results ?: emptyList()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "未知错误"
                    throw Exception("搜索失败: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                errorMessage = "搜索失败: ${e.message}"
                Log.e("SearchResultScreen", "搜索错误", e)
            } finally {
                isLoading = false
            }
        }
    }

    // 初始化时执行一次搜索
    LaunchedEffect(Unit) {
        if (!query.isNullOrEmpty()) {
            performSearch()
        }
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

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "搜索失败",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 18.sp
                        )
                    }
                }
                searchResults.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { result ->
                            SearchResultCard(result)
                        }
                    }
                }
                else -> {
                    // 没有搜索结果时的提示
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchText.isNotEmpty()) "没有找到相关文档" else "请输入搜索内容",
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// 搜索结果卡片组件
@Composable
fun SearchResultCard(result: SearchResultResponse) {
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
                    text = result.filename,
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