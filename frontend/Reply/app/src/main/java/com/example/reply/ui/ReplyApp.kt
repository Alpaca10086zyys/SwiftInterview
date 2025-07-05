// ReplyApp.kt
package com.example.reply.ui

import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import com.example.reply.data.UserData
import com.example.reply.ui.daliyquestion.DailyQuestionPage
import com.example.reply.ui.homepage.GroupsScreen
import com.example.reply.ui.homepage.LoginScreen
import com.example.reply.ui.homepage.RegisterScreen
import com.example.reply.ui.interview.InterviewScreen
import com.example.reply.ui.knowledgebase.KnowledgeBaseScreen
import com.example.reply.ui.knowledgebase.SearchResultScreen
import com.example.reply.ui.knowledgebase.UploadScreen
import com.example.reply.ui.navigation.ReplyNavigationActions
import com.example.reply.ui.navigation.ReplyNavigationWrapper
import com.example.reply.ui.navigation.Route
import com.example.reply.ui.utils.DevicePosture
import com.example.reply.ui.utils.ReplyContentType
import com.example.reply.ui.utils.ReplyNavigationType
import com.example.reply.ui.utils.isBookPosture
import com.example.reply.ui.utils.isSeparating

private fun NavigationSuiteType.toReplyNavType() = when (this) {
    NavigationSuiteType.NavigationBar -> ReplyNavigationType.BOTTOM_NAVIGATION
    NavigationSuiteType.NavigationRail -> ReplyNavigationType.NAVIGATION_RAIL
    NavigationSuiteType.NavigationDrawer -> ReplyNavigationType.PERMANENT_NAVIGATION_DRAWER
    else -> ReplyNavigationType.BOTTOM_NAVIGATION
}

@Composable
fun ReplyApp(
    windowSize: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    replyHomeUIState: ReplyHomeUIState,
    closeDetailScreen: () -> Unit = {},
    navigateToDetail: (Long, ReplyContentType) -> Unit = { _, _ -> },
    toggleSelectedEmail: (Long) -> Unit = { },
) {
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()

    val foldingDevicePosture = when {
        isBookPosture(foldingFeature) ->
            DevicePosture.BookPosture(foldingFeature.bounds)

        isSeparating(foldingFeature) ->
            DevicePosture.Separating(foldingFeature.bounds, foldingFeature.orientation)

        else -> DevicePosture.NormalPosture
    }

    val contentType = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> ReplyContentType.SINGLE_PANE
        WindowWidthSizeClass.Medium -> if (foldingDevicePosture != DevicePosture.NormalPosture) {
            ReplyContentType.DUAL_PANE
        } else {
            ReplyContentType.SINGLE_PANE
        }
        WindowWidthSizeClass.Expanded -> ReplyContentType.DUAL_PANE
        else -> ReplyContentType.SINGLE_PANE
    }

    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        ReplyNavigationActions(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 管理用户状态
    var groupsUserData by remember { mutableStateOf<UserData?>(null) }

    Surface {
        ReplyNavigationWrapper(
            currentDestination = currentDestination,
            navigateToTopLevelDestination = navigationActions::navigateTo,
        ) {
            ReplyNavHost(
                navController = navController,
                contentType = contentType,
                displayFeatures = displayFeatures,
                replyHomeUIState = replyHomeUIState,
                navigationType = navSuiteType.toReplyNavType(),
                closeDetailScreen = closeDetailScreen,
                navigateToDetail = navigateToDetail,
                toggleSelectedEmail = toggleSelectedEmail,
                groupsUserData = groupsUserData,
                onGroupsUserDataChange = { user -> groupsUserData = user }
            )
        }
    }
}

@Composable
private fun ReplyNavHost(
    navController: NavHostController,
    contentType: ReplyContentType,
    displayFeatures: List<DisplayFeature>,
    replyHomeUIState: ReplyHomeUIState,
    navigationType: ReplyNavigationType,
    closeDetailScreen: () -> Unit,
    navigateToDetail: (Long, ReplyContentType) -> Unit,
    toggleSelectedEmail: (Long) -> Unit,
    groupsUserData: UserData?,
    onGroupsUserDataChange: (UserData?) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Inbox,
    ) {
        composable<Route.Inbox> { InterviewScreen() }
        composable<Route.DirectMessages> { EmptyComingSoon() }
        composable<Route.Book> {
            KnowledgeBaseScreen(navController, groupsUserData?.id ?: "")
        }
        composable<Route.Articles> { DailyQuestionPage() }
        composable<Route.Groups> {
            GroupsScreen(
                navController = navController,
                userData = groupsUserData,
                onLogout = { onGroupsUserDataChange(null) }
            )
        }
        composable<Route.Login> {
            LoginScreen(
                onBackClicked = { navController.popBackStack() },
                onRegisterClicked = { navController.navigate(Route.Register) },
                onLoginSuccess = { user: UserData ->
                    onGroupsUserDataChange(user)
                    navController.popBackStack() // 返回到上一个页面（Groups）
                }
            )
        }
        composable<Route.Register> {
            RegisterScreen(
                onBackClicked = { navController.popBackStack() },
                onLoginClicked = { navController.navigate(Route.Login) },
                onRegisterSuccess = {
                    // 注册成功后导航到登录页面
                    navController.navigate(Route.Login) {
                        // 清除注册页面
                        popUpTo(Route.Register) { inclusive = true }
                    }
                }
            )
        }

        // 添加上传屏幕的路由定义（使用字符串路由）
        composable(
            route = "upload/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UploadScreen(navController, userId)
        }

        composable(
            route = "searchResult/{userId}/{query}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("query") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultScreen(navController, userId, query)
        }
    }
}