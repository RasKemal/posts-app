package com.example.postsapplication.data.mapper

import com.example.postsapplication.data.local.entity.PostEntity
import com.example.postsapplication.data.remote.dto.PostDto
import com.example.postsapplication.domain.model.Post

// Seeded by post id (not list position) so a post keeps its image after deletes or filtering.
fun postImageUrl(id: Int): String = "https://picsum.photos/300/300?random=$id&grayscale"

fun PostDto.toEntity(): PostEntity = PostEntity(id = id, userId = userId, title = title, body = body)

fun PostEntity.toDto(): PostDto = PostDto(userId = userId, id = id, title = title, body = body)

fun PostEntity.toDomain(): Post = Post(
    id = id,
    title = title,
    body = body,
    imageUrl = postImageUrl(id),
    isEdited = isLocallyModified,
)
