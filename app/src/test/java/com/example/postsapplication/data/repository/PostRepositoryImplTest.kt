package com.example.postsapplication.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.postsapplication.data.local.PostsDatabase
import com.example.postsapplication.data.local.dao.PostDao
import com.example.postsapplication.data.remote.PostApi
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@RunWith(RobolectricTestRunner::class)
class PostRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val server = MockWebServer()
    private lateinit var database: PostsDatabase
    private lateinit var postDao: PostDao

    @Before
    fun setUp() {
        server.start()
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PostsDatabase::class.java,
        ).allowMainThreadQueries().build()
        postDao = database.postDao()
    }

    @After
    fun tearDown() {
        database.close()
        server.close()
    }

    @Test
    fun `refresh stores posts from the server`() = runTest {
        server.enqueue(postsResponse(ids = 1..3))

        val result = repository().refresh()

        assertTrue(result is DataResult.Success)
        assertEquals(3, postDao.count())
        assertEquals("title 1", postDao.getPost(1)?.title)
    }

    @Test
    fun `second refresh revalidates with the ETag and skips the write on 304`() = runTest {
        val repository = repository()
        server.enqueue(postsResponse(ids = 1..3))
        repository.refresh()
        server.takeRequest()
        // Change the cached row so we can tell whether the 304 rewrote it.
        postDao.updateFromRemote(id = 1, userId = 1, title = "unchanged", body = "body")

        server.enqueue(MockResponse.Builder().code(304).addHeader("ETag", ETAG).build())
        val result = repository.refresh()

        assertTrue(result is DataResult.Success)
        assertEquals(ETAG, server.takeRequest().headers["If-None-Match"])
        assertEquals("unchanged", postDao.getPost(1)?.title)
    }

    @Test
    fun `refresh keeps local edits and deletes`() = runTest {
        val repository = repository()
        server.enqueue(postsResponse(ids = 1..3))
        repository.refresh()
        postDao.updateContent(1, title = "my edit", body = "my body")
        postDao.markDeleted(2)

        server.enqueue(postsResponse(ids = 1..3, etag = "\"v2\""))
        repository.refresh()

        assertEquals("my edit", postDao.getPost(1)?.title)
        assertNull(repository.observePost(2).first())
    }

    @Test
    fun `refresh returns a network error when offline`() = runTest {
        val repository = repository(baseUrl = unreachableUrl())

        val result = repository.refresh()

        assertEquals(DataResult.Failure(DataError.NO_INTERNET), result)
    }

    @Test
    fun `refresh maps server errors`() = runTest {
        server.enqueue(MockResponse.Builder().code(503).build())

        val result = repository().refresh()

        assertEquals(DataResult.Failure(DataError.SERVER_ERROR), result)
    }

    @Test
    fun `updatePost writes to the cache after the server accepts it`() = runTest {
        val repository = seededRepository()
        server.enqueue(MockResponse.Builder().code(200).body(postJson(1, "new title")).build())

        val result = repository.updatePost(1, title = "new title", body = "new body")

        assertTrue(result is DataResult.Success)
        assertEquals("PUT", server.takeRequest().method)
        val post = repository.observePost(1).first()!!
        assertEquals("new title", post.title)
        assertEquals("new body", post.body)
        assertTrue(postDao.getPost(1)!!.isLocallyModified)
    }

    @Test
    fun `updatePost leaves the cache unchanged when the server fails`() = runTest {
        val repository = seededRepository()
        server.enqueue(MockResponse.Builder().code(500).build())

        val result = repository.updatePost(1, title = "new title", body = "new body")

        assertEquals(DataResult.Failure(DataError.SERVER_ERROR), result)
        val post = postDao.getPost(1)!!
        assertEquals("title 1", post.title)
        assertFalse(post.isLocallyModified)
    }

    @Test
    fun `updatePost returns not found for an unknown post`() = runTest {
        val result = repository().updatePost(99, title = "t", body = "b")

        assertEquals(DataResult.Failure(DataError.NOT_FOUND), result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `deletePost removes the post after the server accepts it`() = runTest {
        val repository = seededRepository()
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())

        val result = repository.deletePost(1)

        assertTrue(result is DataResult.Success)
        assertEquals("DELETE", server.takeRequest().method)
        assertNull(repository.observePost(1).first())
    }

    @Test
    fun `deletePost keeps the post when the server fails`() = runTest {
        val repository = seededRepository()
        server.enqueue(MockResponse.Builder().code(500).build())

        val result = repository.deletePost(1)

        assertEquals(DataResult.Failure(DataError.SERVER_ERROR), result)
        assertEquals(1, repository.observePost(1).first()?.id)
    }

    @Test
    fun `deletePost keeps the post when offline`() = runTest {
        val seeded = seededRepository()
        val offline = repository(baseUrl = unreachableUrl())

        val result = offline.deletePost(1)

        assertEquals(DataResult.Failure(DataError.NO_INTERNET), result)
        assertEquals(1, seeded.observePost(1).first()?.id)
    }

    private suspend fun TestScope.seededRepository(): PostRepositoryImpl {
        val repository = repository()
        server.enqueue(postsResponse(ids = 1..3))
        repository.refresh()
        server.takeRequest()
        return repository
    }

    private fun TestScope.repository(baseUrl: String = server.url("/").toString()): PostRepositoryImpl {
        val client = OkHttpClient.Builder()
            .cache(Cache(tempFolder.newFolder(), 1024 * 1024))
            .build()
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PostApi::class.java)
        return PostRepositoryImpl(
            api = api,
            postDao = postDao,
            appScope = backgroundScope,
        )
    }

    private fun unreachableUrl(): String {
        val closed = MockWebServer().apply { start() }
        val url = closed.url("/").toString()
        closed.close()
        return url
    }

    private fun postsResponse(ids: IntRange, etag: String = ETAG) =
        MockResponse.Builder()
            .code(200)
            .addHeader("ETag", etag)
            .addHeader("Cache-Control", "max-age=43200")
            .body(ids.joinToString(prefix = "[", postfix = "]") { postJson(it, "title $it") })
            .build()

    private fun postJson(id: Int, title: String) =
        """{"userId":1,"id":$id,"title":"$title","body":"body $id"}"""

    private companion object {
        const val ETAG = "W/\"v1\""
    }
}
