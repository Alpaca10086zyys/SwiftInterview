// ReplyNavigationActions.kt
package com.example.reply.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.reply.R
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Inbox : Route
    @Serializable data object DirectMessages : Route
    @Serializable data object Book : Route
    @Serializable data object Articles : Route
    @Serializable data object Groups : Route
    @Serializable data object Login : Route
    @Serializable data object Register : Route
    // 添加上传路由对象
    @Serializable data object Upload : Route
    @Serializable data object ReviewDetail : Route


}

data class ReplyTopLevelDestination(val route: Route, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val iconTextId: Int)

class ReplyNavigationActions(private val navController: NavHostController) {

    fun navigateTo(destination: ReplyTopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // 修复上传导航方法
    fun navigateToUpload(userId: String) {
        // 使用正确的路由格式
        navController.navigate("upload/$userId") {
            launchSingleTop = true
        }
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    ReplyTopLevelDestination(
        route = Route.Inbox,
        selectedIcon = Icons.Default.Face,
        unselectedIcon = Icons.Default.Face,
        iconTextId = R.string.tab_inbox,
    ),
    ReplyTopLevelDestination(
        route = Route.DirectMessages,
        selectedIcon = Icons.Outlined.ChatBubbleOutline,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
        iconTextId = R.string.tab_inbox,
    ),
    ReplyTopLevelDestination(
        route = Route.Book,
        selectedIcon = Icons.Default.Book,
        unselectedIcon = Icons.Default.Book,
        iconTextId = R.string.book_title,
    ),
    ReplyTopLevelDestination(
        route = Route.Articles,
        selectedIcon = Icons.AutoMirrored.Filled.Article,
        unselectedIcon = Icons.AutoMirrored.Filled.Article,
        iconTextId = R.string.tab_article,
    ),
    ReplyTopLevelDestination(
        route = Route.Groups,
        selectedIcon = Icons.Default.People,
        unselectedIcon = Icons.Default.People,
        iconTextId = R.string.tab_article,
    )
)