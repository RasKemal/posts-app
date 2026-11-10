package com.example.postsapplication.ui.common

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any,
    ) : UiText {
        override fun equals(other: Any?): Boolean =
            other is StringResource && resId == other.resId && args.contentEquals(other.args)

        override fun hashCode(): Int = 31 * resId + args.contentHashCode()
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId, *args)
    }

    fun asString(resources: Resources): String = when (this) {
        is DynamicString -> value
        is StringResource -> resources.getString(resId, *args)
    }
}
