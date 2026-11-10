package com.example.postsapplication.domain.usecase

import com.example.postsapplication.domain.result.UpdatePostResult
import com.example.postsapplication.testing.FakePostRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePostUseCaseTest {

    private val repository = FakePostRepository()
    private val updatePost = UpdatePostUseCase(repository)

    @Test
    fun `blank title is rejected without calling the repository`() = runTest {
        val result = updatePost(1, title = "   ", body = "body")

        assertEquals(UpdatePostResult.BlankTitle, result)
        assertTrue(repository.updateCalls.isEmpty())
    }

    @Test
    fun `title and body are trimmed`() = runTest {
        val result = updatePost(1, title = "  title ", body = " body  ")

        assertEquals(UpdatePostResult.Success, result)
        assertEquals(listOf(Triple(1, "title", "body")), repository.updateCalls)
    }
}
