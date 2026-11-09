package com.example.postsapplication.data.repository

import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs [block] and turns its outcome into a [DataResult]: its return value on success,
 * or the matching [DataError] if it throws.
 */
suspend fun <T> runDataOperation(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: CancellationException) {
        throw e // Cancellation must reach the coroutine, never turn into an error.
    } catch (e: Exception) {
        DataResult.Failure(e.toDataError())
    }

fun Throwable.toDataError(): DataError = when (this) {
    is IOException -> DataError.NO_INTERNET // includes timeouts
    is HttpException -> when (code()) {
        404 -> DataError.NOT_FOUND
        in 500..599 -> DataError.SERVER_ERROR
        else -> DataError.UNKNOWN
    }
    else -> DataError.UNKNOWN
}
