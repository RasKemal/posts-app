package com.example.postsapplication.domain.result

import com.example.postsapplication.domain.error.DataError

/**
 * Outcome of a data operation: [Success] carries the data, [Failure] says what went wrong.
 * Operations with nothing to return use `DataResult<Unit>`.
 *
 * `out T` lets a [Failure] (a `DataResult<Nothing>`) be returned where any `DataResult<T>` is expected.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}
