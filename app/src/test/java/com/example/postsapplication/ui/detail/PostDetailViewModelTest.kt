package com.example.postsapplication.ui.detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.postsapplication.R
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.domain.model.Post
import com.example.postsapplication.domain.result.DataResult
import com.example.postsapplication.domain.usecase.UpdatePostUseCase
import com.example.postsapplication.testing.FakePostRepository
import com.example.postsapplication.testing.MainDispatcherRule
import com.example.postsapplication.ui.common.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PostDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePostRepository().apply {
        posts[1] = Post(id = 1, title = "title", body = "body", imageUrl = "url", isEdited = false)
    }

    private fun TestScope.viewModel(id: Int = 1): PostDetailViewModel {
        val viewModel = PostDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to id)),
            repository = repository,
            updatePost = UpdatePostUseCase(repository),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun PostDetailViewModel.type(title: String? = null, body: String? = null) {
        title?.let { titleState.setTextAndPlaceCursorAtEnd(it) }
        body?.let { bodyState.setTextAndPlaceCursorAtEnd(it) }
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun `loads the post into the fields`() = runTest {
        val viewModel = viewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("url", state.imageUrl)
        assertEquals("title", viewModel.titleState.text.toString())
        assertEquals("body", viewModel.bodyState.text.toString())
        assertFalse(state.hasChanges)
        assertFalse(state.canSave)
    }

    @Test
    fun `unknown post shows not found`() = runTest {
        val state = viewModel(id = 99).uiState.value

        assertTrue(state.isNotFound)
        assertFalse(state.isLoading)
    }

    @Test
    fun `editing enables save`() = runTest {
        val viewModel = viewModel()

        viewModel.type(title = "new title")

        assertTrue(viewModel.uiState.value.hasChanges)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `whitespace-only changes are not changes`() = runTest {
        val viewModel = viewModel()

        viewModel.type(title = "title  ")

        assertFalse(viewModel.uiState.value.hasChanges)
    }

    @Test
    fun `blank title shows an error and disables save`() = runTest {
        val viewModel = viewModel()

        viewModel.type(title = "  ")

        assertTrue(viewModel.uiState.value.isTitleBlank)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `successful save sends trimmed text and navigates back`() = runTest {
        val viewModel = viewModel()
        viewModel.type(title = " new title ", body = "new body ")

        viewModel.events.test {
            viewModel.onAction(PostDetailAction.Save)

            assertEquals(PostDetailEvent.NavigateBack, awaitItem())
        }
        assertEquals(listOf(Triple(1, "new title", "new body")), repository.updateCalls)
    }

    @Test
    fun `failed save keeps the edits and shows a message`() = runTest {
        repository.updateResult = DataResult.Failure(DataError.NO_INTERNET)
        val viewModel = viewModel()
        viewModel.type(title = "new title")

        viewModel.events.test {
            viewModel.onAction(PostDetailAction.Save)

            assertEquals(
                PostDetailEvent.ShowMessage(UiText.StringResource(R.string.error_no_internet_title)),
                awaitItem(),
            )
        }
        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(viewModel.uiState.value.canSave)
        assertEquals("new title", viewModel.titleState.text.toString())
    }

    @Test
    fun `back without changes navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(PostDetailAction.Back)

            assertEquals(PostDetailEvent.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `back with changes asks before discarding`() = runTest {
        val viewModel = viewModel()
        viewModel.type(body = "changed")

        viewModel.onAction(PostDetailAction.Back)
        assertTrue(viewModel.uiState.value.showDiscardDialog)

        viewModel.events.test {
            viewModel.onAction(PostDetailAction.ConfirmDiscard)

            assertEquals(PostDetailEvent.NavigateBack, awaitItem())
        }
        assertFalse(viewModel.uiState.value.showDiscardDialog)
    }

    @Test
    fun `keep editing closes the dialog and stays`() = runTest {
        val viewModel = viewModel()
        viewModel.type(body = "changed")
        viewModel.onAction(PostDetailAction.Back)

        viewModel.events.test {
            viewModel.onAction(PostDetailAction.DismissDiscard)

            expectNoEvents()
        }
        assertFalse(viewModel.uiState.value.showDiscardDialog)
        assertFalse(viewModel.uiState.value.isTitleBlank)
    }
}
