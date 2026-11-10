package com.example.postsapplication.domain.result

sealed interface UpdatePostResult {
    data object Success : UpdatePostResult
    data object BlankTitle : UpdatePostResult
}
