package com.example.postsapplication.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object PostListRoute

@Serializable
data class PostDetailRoute(val id: Int)
