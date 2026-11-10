package com.example.postsapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.postsapplication.ui.screens.detail.navigateToPostDetail
import com.example.postsapplication.ui.screens.detail.postDetailScreen
import com.example.postsapplication.ui.screens.list.postListScreen

@Composable
fun PostsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = PostListRoute,
        modifier = modifier,
    ) {
        postListScreen(onPostClick = navController::navigateToPostDetail)
        postDetailScreen(onBack = { navController.navigateUp() })
    }
}
