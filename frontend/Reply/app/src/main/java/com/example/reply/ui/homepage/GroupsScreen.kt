package com.example.reply.ui.homepage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.reply.ui.navigation.Route

// 定义页面枚举
enum class GroupsPage {
    Main,
    Login,
    Register
}

@Composable
fun GroupsScreen(navController: NavHostController) {
    // 使用状态管理当前页面
    var currentPage by remember { mutableStateOf(GroupsPage.Main) }

    when (currentPage) {
        GroupsPage.Main -> GroupsMainPage(
            onLoginClicked = { currentPage = GroupsPage.Login }
        )
        GroupsPage.Login -> LoginScreen(
            onBackClicked = { currentPage = GroupsPage.Main },
            onRegisterClicked = { currentPage = GroupsPage.Register }
        )
        GroupsPage.Register -> RegisterScreen(
            onBackClicked = { currentPage = GroupsPage.Login },
            onLoginClicked = { currentPage = GroupsPage.Login }
        )
    }
}