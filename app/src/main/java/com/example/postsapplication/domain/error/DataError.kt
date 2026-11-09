package com.example.postsapplication.domain.error

enum class DataError {
    /** No connection, or the request timed out. */
    NO_INTERNET,

    /** HTTP 404, or the post isn't in the local database. */
    NOT_FOUND,

    /** HTTP 5xx. */
    SERVER_ERROR,

    /** Anything else: other HTTP codes, unreadable response, database error. */
    UNKNOWN,
}
