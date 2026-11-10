package com.example.postsapplication.ui.screens.list.model

import androidx.compose.runtime.Immutable
import com.example.postsapplication.domain.model.Post

@Immutable
data class PostListItemUiModel(
    val id: Int,
    val title: String,
    val bodyPreview: String,
    val imageUrl: String,
)

private val LINE_BREAKS = Regex("\\s*\\R\\s*")

fun Post.toListItemUiModel(): PostListItemUiModel = PostListItemUiModel(
    id = id,
    title = title,
    bodyPreview = body.trim().replace(LINE_BREAKS, " "),
    imageUrl = imageUrl,
)
