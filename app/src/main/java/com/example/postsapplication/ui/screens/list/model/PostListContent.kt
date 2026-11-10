package com.example.postsapplication.ui.screens.list.model

import androidx.paging.LoadState
import com.example.postsapplication.domain.error.DataError

sealed interface PostListContent {
    data object Posts : PostListContent
    data class Loading(val isSearching: Boolean) : PostListContent
    data object Empty : PostListContent
    data class NoResults(val query: String) : PostListContent
    data class Error(val error: DataError) : PostListContent
}

fun postListContent(
    itemCount: Int,
    localRefresh: LoadState,
    query: String,
    isSyncing: Boolean,
    syncError: DataError?,
): PostListContent {
    val activeQuery = query.trim()
    return when {
        itemCount > 0 -> PostListContent.Posts
        localRefresh is LoadState.Loading -> PostListContent.Loading(isSearching = activeQuery.isNotEmpty())
        activeQuery.isNotEmpty() -> PostListContent.NoResults(activeQuery)
        isSyncing -> PostListContent.Loading(isSearching = false)
        syncError != null -> PostListContent.Error(syncError)
        else -> PostListContent.Empty
    }
}
