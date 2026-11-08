package com.example.postsapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.postsapplication.ui.navigation.PostsNavHost

@Composable
fun PostsApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    PostsNavHost(
        navController = navController,
        modifier = modifier,
    )
}
