package com.example.postsapplication.ui.list

import androidx.paging.LoadState
import com.example.postsapplication.domain.error.DataError
import org.junit.Assert.assertEquals
import org.junit.Test

class PostListContentTest {

    private val loaded = LoadState.NotLoading(endOfPaginationReached = true)

    private fun content(
        itemCount: Int = 0,
        localRefresh: LoadState = loaded,
        query: String = "",
        isSyncing: Boolean = false,
        syncError: DataError? = null,
    ) = postListContent(itemCount, localRefresh, query, isSyncing, syncError)

    @Test
    fun `posts win over any sync state`() {
        assertEquals(
            PostListContent.Posts,
            content(itemCount = 3, isSyncing = true, syncError = DataError.Network.NO_INTERNET),
        )
    }

    @Test
    fun `loading while Room is loading`() {
        assertEquals(PostListContent.Loading(isSearching = false), content(localRefresh = LoadState.Loading))
        assertEquals(
            PostListContent.Loading(isSearching = true),
            content(localRefresh = LoadState.Loading, query = "qui"),
        )
    }

    @Test
    fun `loading while the first sync runs on an empty cache`() {
        assertEquals(PostListContent.Loading(isSearching = false), content(isSyncing = true))
    }

    @Test
    fun `no results for a search with no matches`() {
        assertEquals(PostListContent.NoResults("qui"), content(query = "  qui "))
    }

    @Test
    fun `error when the sync failed and nothing is cached`() {
        assertEquals(
            PostListContent.Error(DataError.Network.NO_INTERNET),
            content(syncError = DataError.Network.NO_INTERNET),
        )
    }

    @Test
    fun `empty when the server has no posts`() {
        assertEquals(PostListContent.Empty, content())
    }
}
