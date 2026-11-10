package com.example.postsapplication.domain.repository

import androidx.paging.PagingData
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun pagedPosts(query: String?): Flow<PagingData<Post>>
    fun observePost(id: Int): Flow<Post?>
    suspend fun refresh(): DataResult<Unit>
    suspend fun updatePost(id: Int, title: String, body: String): DataResult<Unit>
    suspend fun deletePost(id: Int): DataResult<Unit>
}
