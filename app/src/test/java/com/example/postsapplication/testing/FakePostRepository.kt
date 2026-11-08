package com.example.postsapplication.testing

import androidx.paging.PagingData
import com.example.postsapplication.core.common.EmptyResult
import com.example.postsapplication.core.common.Result
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.repository.PostRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePostRepository : PostRepository {

    var refreshResult: EmptyResult<DataError> = Result.Success(Unit)
    var updateResult: EmptyResult<DataError> = Result.Success(Unit)
    var deleteResult: EmptyResult<DataError> = Result.Success(Unit)

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

    override fun observePost(id: Int): Flow<Post?> = flowOf(null)

    override suspend fun refresh(): EmptyResult<DataError> {
        refreshCalls++
        refreshGate?.await()
        return refreshResult
    }

    override suspend fun updatePost(id: Int, title: String, body: String): EmptyResult<DataError> {
        updateCalls += Triple(id, title, body)
        return updateResult
    }

    override suspend fun deletePost(id: Int): EmptyResult<DataError> {
        deleteCalls += id
        return deleteResult
    }
}
