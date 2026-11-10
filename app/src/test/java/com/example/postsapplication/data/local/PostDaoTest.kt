package com.example.postsapplication.data.local

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.postsapplication.data.local.dao.PostDao
import com.example.postsapplication.data.local.entity.PostEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PostDaoTest {

    private lateinit var database: PostsDatabase
    private lateinit var dao: PostDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PostsDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.postDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `replaceWithRemote inserts new posts`() = runTest {
        dao.replaceWithRemote(listOf(post(1), post(2)))

        assertEquals(2, dao.count())
        assertEquals("title 1", dao.getPost(1)?.title)
    }

    @Test
    fun `replaceWithRemote updates untouched posts`() = runTest {
        dao.replaceWithRemote(listOf(post(1)))
        dao.replaceWithRemote(listOf(post(1, title = "server update")))

        assertEquals("server update", dao.getPost(1)?.title)
    }

    @Test
    fun `replaceWithRemote keeps local edits`() = runTest {
        dao.replaceWithRemote(listOf(post(1)))
        dao.updateContent(1, title = "my edit", body = "my body")

        dao.replaceWithRemote(listOf(post(1, title = "server update")))

        val stored = dao.getPost(1)!!
        assertEquals("my edit", stored.title)
        assertEquals("my body", stored.body)
        assertTrue(stored.isLocallyModified)
    }

    @Test
    fun `replaceWithRemote does not bring back deleted posts`() = runTest {
        dao.replaceWithRemote(listOf(post(1)))
        dao.markDeleted(1)

        dao.replaceWithRemote(listOf(post(1)))

        assertTrue(dao.getPost(1)!!.isDeleted)
    }

    @Test
    fun `replaceWithRemote removes posts missing from the server`() = runTest {
        dao.replaceWithRemote(listOf(post(1), post(2)))

        dao.replaceWithRemote(listOf(post(2)))

        assertNull(dao.getPost(1))
        assertEquals(1, dao.count())
    }

    @Test
    fun `pagingSource hides deleted posts and orders by id`() = runTest {
        dao.replaceWithRemote(listOf(post(3), post(1), post(2)))
        dao.markDeleted(2)

        assertEquals(listOf(1, 3), loadIds(query = null))
    }

    @Test
    fun `pagingSource filters by title or body`() = runTest {
        dao.replaceWithRemote(
            listOf(
                post(1, title = "qui est esse", body = "lorem"),
                post(2, title = "dolor", body = "QUI in body"),
                post(3, title = "nothing", body = "here"),
            ),
        )

        assertEquals(listOf(1, 2), loadIds(query = "qui"))
    }

    @Test
    fun `pagingSource loads in pages`() = runTest {
        dao.replaceWithRemote((1..45).map { post(it) })
        val config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false)
        val pager = TestPager(config, dao.pagingSource(null))

        val first = pager.refresh() as PagingSource.LoadResult.Page
        val second = pager.append() as PagingSource.LoadResult.Page

        assertEquals((1..20).toList() + (21..40).toList(), (first.data + second.data).map { it.id })
    }

    private suspend fun loadIds(query: String?): List<Int> {
        val pager = TestPager(PagingConfig(pageSize = 20, enablePlaceholders = false), dao.pagingSource(query))
        val page = pager.refresh() as PagingSource.LoadResult.Page
        return page.data.map { it.id }
    }

    private fun post(id: Int, title: String = "title $id", body: String = "body $id") =
        PostEntity(id = id, userId = 1, title = title, body = body)
}
