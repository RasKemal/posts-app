package com.example.postsapplication.data.repository

import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.result.DataResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

internal suspend fun <T> runDataOperation(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Failure(e.toDataError())
    }

internal fun Throwable.toDataError(): DataError = when (this) {
    is IOException -> DataError.NO_INTERNET
    is HttpException -> when (code()) {
        404 -> DataError.NOT_FOUND
        in 500..599 -> DataError.SERVER_ERROR
        else -> DataError.UNKNOWN
    }
    else -> DataError.UNKNOWN
}
