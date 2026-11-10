package com.example.postsapplication.ui.list

import androidx.compose.runtime.Immutable
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.ui.common.UiText

@Immutable
data class PostListUiState(
    val query: String = "",
    /** Pull-to-refresh indicator; only for refreshes the user started. */
    val isRefreshing: Boolean = false,
    /** Any sync with the server is running (also the automatic one on start). */
    val isSyncing: Boolean = false,
    /** Last sync error, used for the full-screen error when there is nothing cached. */
    val syncError: DataError? = null,
    /** Posts whose delete request is in flight (or already succeeded). */
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
