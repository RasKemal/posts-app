package com.example.postsapplication.testing

import androidx.paging.PagingData
import com.example.postsapplication.domain.model.Comment
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.repository.PostRepository
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePostRepository : PostRepository {

    var refreshResult: DataResult<Unit> = DataResult.Success(Unit)

    /** When set, refresh suspends until it is completed, to observe in-progress state. */
    var refreshGate: CompletableDeferred<Unit>? = null

    var refreshCalls = 0
        private set
    val updateCalls = mutableListOf<Triple<Int, String, String>>()
    val deleteCalls = mutableListOf<Int>()
    val pagedQueries = mutableListOf<String?>()

    override fun pagedPosts(query: String?): Flow<PagingData<Post>> {
        pagedQueries += query
        return flowOf(PagingData.empty())
    }

    val posts = mutableMapOf<Int, Post>()

    override suspend fun getPost(id: Int): Post? = posts[id]

    override suspend fun refresh(): DataResult<Unit> {
        refreshCalls++
        refreshGate?.await()
        return refreshResult
    }

    override suspend fun updatePost(id: Int, title: String, body: String) {
        updateCalls += Triple(id, title, body)
    }

    override suspend fun deletePost(id: Int) {
        deleteCalls += id
    }
}
