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
            navController.navigate(Route.Login)
        },
        userData = userData,
        onLogout = onLogout,

        onAccountClick = {
            navController.navigate(Route.Account)
        },
        onHelpClick = {
            navController.navigate(Route.Help)
        },
        onRateClick = {
            navController.navigate(Route.Rate)
        }
    )
}
