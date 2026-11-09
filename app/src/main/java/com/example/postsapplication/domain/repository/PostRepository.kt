package com.example.postsapplication.domain.repository

import androidx.paging.PagingData
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    /** Pages posts from the local cache; [query] filters by title/body, `null` means all. */
    fun pagedPosts(query: String?): Flow<PagingData<Post>>

    fun observePost(id: Int): Flow<Post?>

    /** Syncs the cache with the server (a cheap 304 when nothing changed). */
    suspend fun refresh(): DataResult<Unit>

    /** Server first: the cache is only updated if the server accepts the change. */
    suspend fun updatePost(id: Int, title: String, body: String): DataResult<Unit>

    /** Server first: the post is removed from the cache only if the server deletes it. */
    suspend fun deletePost(id: Int): DataResult<Unit>
}
