package com.example.postsapplication.ui.list

import androidx.compose.runtime.Immutable
import com.example.postsapplication.domain.model.Post

/** What a list row shows; the body is flattened to one line for the 2-line preview. */
@Immutable
data class PostListItem(
    val id: Int,
    val title: String,
    val bodyPreview: String,
    val imageUrl: String,
    val isEdited: Boolean,
)

private val LINE_BREAKS = Regex("\\s*\\R\\s*")

fun Post.toListItem(): PostListItem = PostListItem(
    id = id,
    title = title,
    bodyPreview = body.trim().replace(LINE_BREAKS, " "),
    imageUrl = imageUrl,
    isEdited = isEdited,
)
