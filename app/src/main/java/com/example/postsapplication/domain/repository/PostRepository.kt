package com.example.postsapplication.domain.repository

import androidx.paging.PagingData
import com.example.postsapplication.core.common.EmptyResult
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    /** Pages posts from the local cache; [query] filters by title/body, `null` means all. */
    fun pagedPosts(query: String?): Flow<PagingData<Post>>

    fun observePost(id: Int): Flow<Post?>

    /** Syncs the cache with the server (a cheap 304 when nothing changed). */
    suspend fun refresh(): EmptyResult<DataError>

    /** Server first: the cache is only updated if the server accepts the change. */
    suspend fun updatePost(id: Int, title: String, body: String): EmptyResult<DataError>

    /** Server first: the post is removed from the cache only if the server deletes it. */
    suspend fun deletePost(id: Int): EmptyResult<DataError>
}
