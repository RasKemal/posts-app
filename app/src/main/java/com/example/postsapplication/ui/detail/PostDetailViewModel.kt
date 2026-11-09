package com.example.postsapplication.ui.detail

import androidx.annotation.StringRes
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
import com.example.postsapplication.core.ui.UiText
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.repository.PostRepository
import com.example.postsapplication.domain.result.UpdatePostResult
import com.example.postsapplication.domain.usecase.UpdatePostUseCase
import com.example.postsapplication.ui.navigation.PostDetail
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
    private val updatePost: UpdatePostUseCase,
) : ViewModel() {

    private val postId = savedStateHandle.toRoute<PostDetail>().id

    // Text lives in TextFieldState (synchronous edits) and is saved in SavedStateHandle,
    // so unsaved edits survive rotation and process death.
    val titleState: TextFieldState =
        savedStateHandle.saveable(key = KEY_TITLE, saver = TextFieldState.Saver, init = { TextFieldState() })
    val bodyState: TextFieldState =
        savedStateHandle.saveable(key = KEY_BODY, saver = TextFieldState.Saver, init = { TextFieldState() })

    private val state = MutableStateFlow(DetailState())

    val uiState: StateFlow<PostDetailUiState> = combine(
        state,
        snapshotFlow { titleState.text.toString() },
        snapshotFlow { bodyState.text.toString() },
    ) { state, title, body -> state.toUiState(title, body) }
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
        // Fill the fields only the first time; after process death they hold the restored draft.
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
            when (val result = updatePost(postId, titleState.text.toString(), bodyState.text.toString())) {
                // isSaving stays true so the form doesn't flash enabled while leaving.
                UpdatePostResult.Success -> _events.send(PostDetailEvent.NavigateBack)
                UpdatePostResult.BlankTitle -> onSaveFailed(R.string.error_title_blank)
                is UpdatePostResult.Failure -> onSaveFailed(
                    if (result.error == DataError.NO_INTERNET) R.string.error_no_internet_title else R.string.error_save_failed,
                )
            }
        }
    }

    private fun onBack() {
        val current = state.value
        val hasChanges = current.hasChanges(titleState.text.toString(), bodyState.text.toString())
        if (hasChanges && !current.isSaving) {
            state.update { it.copy(showDiscardDialog = true) }
        } else {
            _events.trySend(PostDetailEvent.NavigateBack)
        }
    }

    private suspend fun onSaveFailed(@StringRes message: Int) {
        state.update { it.copy(isSaving = false) }
        _events.send(PostDetailEvent.ShowMessage(UiText.StringResource(message)))
    }

    private data class DetailState(
        val isLoading: Boolean = true,
        val isNotFound: Boolean = false,
        val imageUrl: String? = null,
        val originalTitle: String = "",
        val originalBody: String = "",
        val isSaving: Boolean = false,
        val showDiscardDialog: Boolean = false,
    ) {
        val isEditable: Boolean get() = !isLoading && !isNotFound

        fun hasChanges(title: String, body: String): Boolean =
            isEditable && (title.trim() != originalTitle.trim() || body.trim() != originalBody.trim())

        fun toUiState(title: String, body: String): PostDetailUiState {
            val hasChanges = hasChanges(title, body)
            val isTitleBlank = isEditable && title.isBlank()
            return PostDetailUiState(
                isLoading = isLoading,
                isNotFound = isNotFound,
                imageUrl = imageUrl,
                titleError = if (isTitleBlank) UiText.StringResource(R.string.error_title_blank) else null,
                hasChanges = hasChanges,
                canSave = hasChanges && !isTitleBlank && !isSaving,
                isSaving = isSaving,
                showDiscardDialog = showDiscardDialog,
            )
        }
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_DRAFT_STARTED = "draft_started"
    }
}
