package com.example.postsapplication.ui.screens.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.navigation.toRoute
import com.example.postsapplication.R
import com.example.postsapplication.domain.repository.PostRepository
import com.example.postsapplication.domain.result.UpdatePostResult
import com.example.postsapplication.domain.usecase.UpdatePostUseCase
import com.example.postsapplication.ui.common.UiText
import com.example.postsapplication.ui.navigation.PostDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PostRepository,
    private val updatePostUseCase: UpdatePostUseCase,
) : ViewModel() {

    private val postId = savedStateHandle.toRoute<PostDetailRoute>().id

    val titleState: TextFieldState =
        savedStateHandle.saveable(key = KEY_TITLE, saver = TextFieldState.Saver, init = { TextFieldState() })
    val bodyState: TextFieldState =
        savedStateHandle.saveable(key = KEY_BODY, saver = TextFieldState.Saver, init = { TextFieldState() })

    private val state = MutableStateFlow(PostDetailUiState())

    val uiState: StateFlow<PostDetailUiState> = combine(
        state,
        snapshotFlow { titleState.text.toString() },
        snapshotFlow { bodyState.text.toString() },
    ) { state, title, body -> state.copy(title = title, body = body) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PostDetailUiState())

    private val _events = Channel<PostDetailEvent>(Channel.BUFFERED)
    val events: Flow<PostDetailEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun onAction(action: PostDetailAction) {
        when (action) {
            PostDetailAction.Save -> save()
            PostDetailAction.Back -> onBack()
            PostDetailAction.ConfirmDiscard -> {
                state.update { it.copy(showDiscardDialog = false) }
                _events.trySend(PostDetailEvent.NavigateBack)
            }
            PostDetailAction.DismissDiscard -> state.update { it.copy(showDiscardDialog = false) }
        }
    }

    private suspend fun load() {
        val post = repository.observePost(postId).first()
        if (post == null) {
            state.update { it.copy(isLoading = false, isNotFound = true) }
            return
        }

        if (savedStateHandle.get<Boolean>(KEY_DRAFT_STARTED) != true) {
            titleState.setTextAndPlaceCursorAtEnd(post.title)
            bodyState.setTextAndPlaceCursorAtEnd(post.body)
            savedStateHandle[KEY_DRAFT_STARTED] = true
        }
        state.update {
            it.copy(isLoading = false, imageUrl = post.imageUrl, originalTitle = post.title, originalBody = post.body)
        }
    }

    private fun save() {
        if (state.value.isSaving) return
        state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (updatePostUseCase(postId, titleState.text.toString(), bodyState.text.toString())) {
                UpdatePostResult.Success -> _events.send(PostDetailEvent.NavigateBack)
                UpdatePostResult.BlankTitle -> {
                    state.update { it.copy(isSaving = false) }
                    _events.send(PostDetailEvent.ShowMessage(UiText.StringResource(R.string.error_title_blank)))
                }
            }
        }
    }

    private fun onBack() {
        val current = state.value.copy(title = titleState.text.toString(), body = bodyState.text.toString())
        if (current.hasChanges && !current.isSaving) {
            state.update { it.copy(showDiscardDialog = true) }
        } else {
            _events.trySend(PostDetailEvent.NavigateBack)
        }
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_DRAFT_STARTED = "draft_started"
    }
}
