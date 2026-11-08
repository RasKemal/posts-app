package com.example.postsapplication.domain.error

import com.example.postsapplication.core.common.RootError

sealed interface DataError : RootError {
    enum class Network : DataError {
        NO_INTERNET,
        REQUEST_TIMEOUT,
        NOT_FOUND,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
    }

    enum class Local : DataError {
        NOT_FOUND,
    }
}
