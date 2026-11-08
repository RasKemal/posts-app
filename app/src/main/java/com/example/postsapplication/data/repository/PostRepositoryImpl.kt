package com.example.postsapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.postsapplication.core.common.ApplicationScope
import com.example.postsapplication.core.common.EmptyResult
import com.example.postsapplication.core.common.Result
import com.example.postsapplication.core.common.asEmptyResult
import com.example.postsapplication.core.common.onSuccess
import com.example.postsapplication.data.local.dao.PostDao
import com.example.postsapplication.data.mapper.toDomain
import com.example.postsapplication.data.mapper.toDto
import com.example.postsapplication.data.mapper.toEntity
import com.example.postsapplication.data.remote.PostApi
import com.example.postsapplication.data.remote.httpCodeToError
import com.example.postsapplication.data.remote.safeApiCall
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.repository.PostRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import javax.inject.Inject

/**
 * Reads are offline-first (Room is the single source of truth), writes are server-first.
 * Writes run in [appScope] so they complete even if the calling screen goes away.
 */
class PostRepositoryImpl @Inject constructor(
    private val api: PostApi,
    private val postDao: PostDao,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : PostRepository {

    override fun pagedPosts(query: String?): Flow<PagingData<Post>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { postDao.pagingSource(query) },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun observePost(id: Int): Flow<Post?> =
        postDao.observePost(id).map { it?.toDomain() }

    override suspend fun refresh(): EmptyResult<DataError> {
        val response = when (val result = safeApiCall { api.getPosts() }) {
            is Result.Error -> return result
            is Result.Success -> result.data
        }
        if (!response.isSuccessful) return Result.Error(httpCodeToError(response.code()))
        val posts = response.body() ?: return Result.Error(DataError.Network.SERIALIZATION)

        // On a 304 the body comes from the HTTP cache and Room already has it.
        val notModified = response.raw().networkResponse?.code == HTTP_NOT_MODIFIED
        if (!notModified || postDao.count() == 0) {
            postDao.replaceWithRemote(posts.map { it.toEntity() })
        }
        return Result.Success(Unit)
    }

    override suspend fun updatePost(id: Int, title: String, body: String): EmptyResult<DataError> =
        appScope.async {
            val post = postDao.getPost(id)
                ?: return@async Result.Error(DataError.Local.NOT_FOUND)
            safeApiCall { api.updatePost(id, post.toDto().copy(title = title, body = body)) }
                .onSuccess { postDao.updateContent(id, title, body) }
                .asEmptyResult()
        }.await()

    override suspend fun deletePost(id: Int): EmptyResult<DataError> =
        appScope.async {
            when (val result = safeApiCall { api.deletePost(id) }) {
                is Result.Error if result.error != DataError.Network.NOT_FOUND -> result
                // Success, or already gone on the server (404): remove it locally.
                else -> {
                    postDao.markDeleted(id)
                    Result.Success(Unit)
                }
            }
        }.await()

    private companion object {
        const val PAGE_SIZE = 20
    }
}
