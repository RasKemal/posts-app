package com.example.postsapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.postsapplication.data.local.dao.PostDao
import com.example.postsapplication.data.mapper.toDomain
import com.example.postsapplication.data.mapper.toDto
import com.example.postsapplication.data.mapper.toEntity
import com.example.postsapplication.data.remote.PostApi
import com.example.postsapplication.di.ApplicationScope
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.repository.PostRepository
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
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

    override suspend fun refresh(): DataResult<Unit> = runDataOperation {
        val response = api.getPosts()
        if (!response.isSuccessful) throw HttpException(response)
        val posts = checkNotNull(response.body())

        // On a 304 the body comes from the HTTP cache and Room already has it.
        val notModified = response.raw().networkResponse?.code == HTTP_NOT_MODIFIED
        if (!notModified || postDao.count() == 0) {
            postDao.replaceWithRemote(posts.map { it.toEntity() })
        }
    }

    override suspend fun updatePost(id: Int, title: String, body: String): DataResult<Unit> =
        appScope.async {
            val post = postDao.getPost(id) ?: return@async DataResult.Failure(DataError.NOT_FOUND)
            runDataOperation {
                api.updatePost(id, post.toDto().copy(title = title, body = body)) // throws on failure…
                postDao.updateContent(id, title, body) // …so the cache only changes after the server accepts
            }
        }.await()

    override suspend fun deletePost(id: Int): DataResult<Unit> =
        appScope.async {
            when (val result = runDataOperation { api.deletePost(id) }) {
                // Already gone on the server (404) counts as deleted.
                is DataResult.Success, DataResult.Failure(DataError.NOT_FOUND) -> {
                    postDao.markDeleted(id)
                    DataResult.Success(Unit)
                }
                is DataResult.Failure -> result
            }
        }.await()

    private companion object {
        const val PAGE_SIZE = 20
    }
}
