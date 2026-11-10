package com.example.postsapplication.ui.common

import com.example.postsapplication.R
import com.example.postsapplication.domain.error.DataError

/** Title + description for full-screen error states. */
data class ErrorMessage(val title: UiText, val description: UiText)

fun DataError.toErrorMessage(): ErrorMessage = when (this) {
    DataError.NO_INTERNET -> ErrorMessage(
        title = UiText.StringResource(R.string.error_no_internet_title),
        description = UiText.StringResource(R.string.error_no_internet_description),
    )
    else -> ErrorMessage(
        title = UiText.StringResource(R.string.error_generic_title),
        description = UiText.StringResource(R.string.error_generic_description),
    )
}
