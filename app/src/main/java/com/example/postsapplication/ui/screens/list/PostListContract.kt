package com.example.postsapplication.ui.screens.list

import androidx.compose.runtime.Immutable
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.ui.common.UiText

@Immutable
data class PostListUiState(
    val query: String = "",
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val syncError: DataError? = null,
    val deletingIds: Set<Int> = emptySet(),
)

sealed interface PostListAction {
    data class QueryChange(val query: String) : PostListAction
    data object Refresh : PostListAction
    data object Retry : PostListAction
    data class Delete(val id: Int) : PostListAction
}

sealed interface PostListEvent {
    data class ShowMessage(val message: UiText) : PostListEvent

    /** The UI shows this as a snackbar only when cached posts are on screen. */
    data object SyncFailed : PostListEvent
}
