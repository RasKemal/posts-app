package com.example.postsapplication.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object PostList

@Serializable
data class PostDetail(val id: Int)
