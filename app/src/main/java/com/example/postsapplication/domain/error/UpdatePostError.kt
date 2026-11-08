package com.example.postsapplication.domain.error

import com.example.postsapplication.core.common.RootError

sealed interface UpdatePostError : RootError {
    data object BlankTitle : UpdatePostError
    data class Failed(val cause: DataError) : UpdatePostError
}
