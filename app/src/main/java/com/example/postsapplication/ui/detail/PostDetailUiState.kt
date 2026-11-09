package com.example.postsapplication.ui.detail

import androidx.compose.runtime.Immutable
import com.example.postsapplication.core.ui.UiText

@Immutable
data class PostDetailUiState(
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val imageUrl: String? = null,
    val titleError: UiText? = null,
    val hasChanges: Boolean = false,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
)

sealed interface PostDetailAction {
    data object Save : PostDetailAction
    data object Back : PostDetailAction
    data object ConfirmDiscard : PostDetailAction
    data object DismissDiscard : PostDetailAction
}

sealed interface PostDetailEvent {
    data object NavigateBack : PostDetailEvent
    data class ShowMessage(val message: UiText) : PostDetailEvent
}
