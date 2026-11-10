package com.example.postsapplication.ui.screens.detail

import androidx.compose.runtime.Immutable
import com.example.postsapplication.ui.common.UiText

@Immutable
data class PostDetailUiState(
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val imageUrl: String? = null,
    val title: String = "",
    val body: String = "",
    val originalTitle: String = "",
    val originalBody: String = "",
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
) {
    val isEditable: Boolean get() = !isLoading && !isNotFound

    val isTitleBlank: Boolean get() = isEditable && title.isBlank()

    val hasChanges: Boolean
        get() = isEditable && (title.trim() != originalTitle.trim() || body.trim() != originalBody.trim())

    val canSave: Boolean get() = hasChanges && !isTitleBlank && !isSaving
}

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
