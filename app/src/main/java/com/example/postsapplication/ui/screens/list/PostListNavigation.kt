package com.example.postsapplication.ui.screens.list

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.postsapplication.ui.navigation.PostListRoute

fun NavGraphBuilder.postListScreen(onPostClick: (Int) -> Unit) {
    composable<PostListRoute> {
        PostListRoot(onPostClick = onPostClick)
    }
}
