package com.example.postsapplication.domain.result

import com.example.postsapplication.domain.error.DataError

sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}
