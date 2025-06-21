package com.example.reply.ui.interview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.foundation.lazy.grid.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInterviewPage(onStartInterview: () -> Unit = {}) {
    var job by remember { mutableStateOf<String?>(null) }
    var style by remember { mutableStateOf<String?>(null) }
    val focusOptions = listOf("项目经历", "基础知识", "行业理解", "应变能力", "沟通表达", "逻辑思维")
    val selectedFocus = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "定制面试",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // 岗位选择
            SectionCard(title = "岗位选择") {
                listOf("前端", "后端", "算法").forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { job = option },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = job == option,
                            onClick = { job = option }
                        )
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 面试官风格
            SectionCard(title = "面试官风格") {
                listOf("亲和型", "中立型", "压力型").forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { style = option },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = style == option,
                            onClick = { style = option }
                        )
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 关注重点
            SectionCard(title = "关注重点") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 200.dp) // 限定高度防止撑满
                ) {
                    items(focusOptions) { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .toggleable(
                                    value = option in selectedFocus,
                                    onValueChange = {
                                        if (option in selectedFocus)
                                            selectedFocus.remove(option)
                                        else
                                            selectedFocus.add(option)
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = option in selectedFocus,
                                onCheckedChange = null
                            )
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 进入按钮
            Button(
                onClick = onStartInterview,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("进入面试")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}
