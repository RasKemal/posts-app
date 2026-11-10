package com.example.postsapplication.ui.screens.detail

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.postsapplication.ui.navigation.PostDetailRoute

private const val TRANSITION_MILLIS = 400

fun NavController.navigateToPostDetail(id: Int) = navigate(PostDetailRoute(id))

fun NavGraphBuilder.postDetailScreen(onBack: () -> Unit) {
    composable<PostDetailRoute>(
        enterTransition = { slideIntoContainer(SlideDirection.Left, tween(TRANSITION_MILLIS)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.Right, tween(TRANSITION_MILLIS)) },
    ) {
        PostDetailRoot(onBack = onBack)
    }
}
