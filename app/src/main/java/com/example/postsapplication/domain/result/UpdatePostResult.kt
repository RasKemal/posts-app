package com.example.postsapplication.domain.result

import com.example.postsapplication.domain.error.DataError

sealed interface UpdatePostResult {
    data object Success : UpdatePostResult
    data object BlankTitle : UpdatePostResult
    data class Failure(val error: DataError) : UpdatePostResult
}
