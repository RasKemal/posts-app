package com.example.postsapplication.core.util

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val NON_SEARCHABLE_CHARS = Regex("[^\\p{L}\\p{N} ]+")
private val WHITESPACE = Regex("\\s+")

fun String.normalizeQuery(): String =
    replace(NON_SEARCHABLE_CHARS, " ").replace(WHITESPACE, " ").trim()

/** Normalizes and debounces search input; a blank query becomes `null`. */
@OptIn(FlowPreview::class)
fun Flow<String>.prepareForSearch(debounceMillis: Long = 300L): Flow<String?> =
    map { it.normalizeQuery() }
        .debounce { query -> if (query.isEmpty()) 0L else debounceMillis }
        .distinctUntilChanged()
        .map { it.ifEmpty { null } }
