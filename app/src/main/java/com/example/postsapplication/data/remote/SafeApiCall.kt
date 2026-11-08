package com.example.postsapplication.data.remote

import com.example.postsapplication.core.common.Result
import com.example.postsapplication.domain.error.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(call: suspend () -> T): Result<T, DataError.Network> =
    try {
        Result.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toNetworkError())
    }

fun Throwable.toNetworkError(): DataError.Network = when (this) {
    is SocketTimeoutException -> DataError.Network.REQUEST_TIMEOUT
    is IOException -> DataError.Network.NO_INTERNET
    is HttpException -> httpCodeToError(code())
    is SerializationException -> DataError.Network.SERIALIZATION
    else -> DataError.Network.UNKNOWN
}

fun httpCodeToError(code: Int): DataError.Network = when (code) {
    404 -> DataError.Network.NOT_FOUND
    408 -> DataError.Network.REQUEST_TIMEOUT
    in 500..599 -> DataError.Network.SERVER_ERROR
    else -> DataError.Network.UNKNOWN
}
