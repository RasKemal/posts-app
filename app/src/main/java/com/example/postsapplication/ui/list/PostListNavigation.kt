package com.example.postsapplication.ui.list

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.postsapplication.ui.navigation.PostList

fun NavGraphBuilder.postListScreen(onPostClick: (Int) -> Unit) {
    composable<PostList> {
        PostListRoute(onPostClick = onPostClick)
    }
}
