package com.example.reply.ui.homepage

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.reply.data.UserData
import com.example.reply.ui.navigation.Route

@Composable
fun GroupsScreen(
    navController: NavHostController,
    userData: UserData? = null,
    onLogout: () -> Unit = {}
) {
    GroupsMainPage(
        onLoginClicked = {
            // 直接使用 Route.Login 对象
            navController.navigate(Route.Login)
        },
        userData = userData,
        onLogout = onLogout
    )
}