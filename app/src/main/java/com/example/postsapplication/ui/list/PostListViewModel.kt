package com.example.postsapplication.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.postsapplication.R
import com.example.postsapplication.core.ui.UiText
import com.example.postsapplication.core.util.prepareForSearch
import com.example.postsapplication.domain.repository.PostRepository
import com.example.postsapplication.domain.result.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostListUiState())
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    private val _events = Channel<PostListEvent>(Channel.BUFFERED)
    val events: Flow<PostListEvent> = _events.receiveAsFlow()

    // Kept apart from uiState: PagingData must not be combined into other state.
    val posts: Flow<PagingData<PostListItem>> = _uiState
        .map { it.query }
        .distinctUntilChanged()
        .prepareForSearch()
        .flatMapLatest { query -> repository.pagedPosts(query) }
        .map { pagingData -> pagingData.map { it.toListItem() } }
        .cachedIn(viewModelScope)

    private var syncJob: Job? = null

    init {
        sync(userInitiated = false)
    }

    fun onAction(action: PostListAction) {
        when (action) {
            is PostListAction.QueryChange -> _uiState.update { it.copy(query = action.query) }
            PostListAction.Refresh -> sync(userInitiated = true)
            PostListAction.Retry -> sync(userInitiated = false)
            is PostListAction.Delete -> delete(action.id)
        }
    }

    private fun sync(userInitiated: Boolean) {
        if (syncJob?.isActive == true) {
            if (userInitiated) _uiState.update { it.copy(isRefreshing = true) }
            return
        }
        _uiState.update { it.copy(isSyncing = true, isRefreshing = userInitiated, syncError = null) }
        syncJob = viewModelScope.launch {
            val result = repository.refresh()
            val error = (result as? DataResult.Failure)?.error
            _uiState.update { it.copy(isSyncing = false, isRefreshing = false, syncError = error) }
            if (error != null) _events.send(PostListEvent.SyncFailed)
        }
    }

    private fun delete(id: Int) {
        if (id in _uiState.value.deletingIds) return
        _uiState.update { it.copy(deletingIds = it.deletingIds + id) }
        viewModelScope.launch {
            if (repository.deletePost(id) is DataResult.Failure) {
                _uiState.update { it.copy(deletingIds = it.deletingIds - id) }
                _events.send(PostListEvent.ShowMessage(UiText.StringResource(R.string.error_delete_failed)))
            }
            // On success the id stays in deletingIds until the row leaves the list.
        }
    }
}
