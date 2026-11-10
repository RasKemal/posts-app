package com.example.postsapplication.ui.list

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryTest {

    @Test
    fun `normalizeQuery strips symbols and collapses whitespace`() {
        assertEquals("qui est esse", "  qui!!  est,   esse? ".normalizeQuery())
    }

    @Test
    fun `prepareForSearch emits null at once for blank input`() = runTest {
        MutableStateFlow("   ").prepareForSearch().test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `prepareForSearch debounces typing and emits only the last query`() = runTest {
        val input = MutableStateFlow("")
        input.prepareForSearch(debounceMillis = 300).test {
            assertEquals(null, awaitItem())

            input.value = "q"
            advanceTimeBy(100)
            input.value = "qu"
            advanceTimeBy(100)
            input.value = "qui"
            expectNoEvents()

            advanceTimeBy(301)
            assertEquals("qui", awaitItem())
        }
    }

    @Test
    fun `prepareForSearch skips queries that normalize to the same value`() = runTest {
        val input = MutableStateFlow("")
        input.prepareForSearch(debounceMillis = 300).test {
            assertEquals(null, awaitItem())
            input.value = "qui"
            advanceTimeBy(301)
            assertEquals("qui", awaitItem())

            input.value = "qui!"
            advanceTimeBy(301)
            expectNoEvents()
        }
    }
}
