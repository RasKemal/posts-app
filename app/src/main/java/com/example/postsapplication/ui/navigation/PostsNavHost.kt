package com.example.postsapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.postsapplication.ui.detail.navigateToPostDetail
import com.example.postsapplication.ui.detail.postDetailScreen
import com.example.postsapplication.ui.list.postListScreen

@Composable
fun PostsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = PostList,
        modifier = modifier,
    ) {
        postListScreen(onPostClick = navController::navigateToPostDetail)
        postDetailScreen(onBack = { navController.navigateUp() })
    }
}
