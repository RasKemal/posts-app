package com.example.postsapplication.ui.list

import app.cash.turbine.test
import com.example.postsapplication.R
import com.example.postsapplication.domain.result.DataResult
import com.example.postsapplication.core.ui.UiText
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.testing.FakePostRepository
import com.example.postsapplication.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePostRepository()

    @Test
    fun `syncs once on start`() = runTest {
        PostListViewModel(repository)

        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `sync state is shown while the first sync runs`() = runTest {
        repository.refreshGate = CompletableDeferred()
        val viewModel = PostListViewModel(repository)

        assertTrue(viewModel.uiState.value.isSyncing)
        assertFalse(viewModel.uiState.value.isRefreshing)

        repository.refreshGate!!.complete(Unit)
        assertFalse(viewModel.uiState.value.isSyncing)
    }

    @Test
    fun `failed sync stores the error and emits SyncFailed`() = runTest {
        repository.refreshResult = DataResult.Failure(DataError.NO_INTERNET)
        val viewModel = PostListViewModel(repository)

        viewModel.events.test {
            assertEquals(PostListEvent.SyncFailed, awaitItem())
        }
        assertEquals(DataError.NO_INTERNET, viewModel.uiState.value.syncError)
    }

    @Test
    fun `pull to refresh shows the indicator until the sync ends`() = runTest {
        val viewModel = PostListViewModel(repository)
        repository.refreshGate = CompletableDeferred()

        viewModel.onAction(PostListAction.Refresh)
        assertTrue(viewModel.uiState.value.isRefreshing)

        repository.refreshGate!!.complete(Unit)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(2, repository.refreshCalls)
    }

    @Test
    fun `retry clears the error and syncs again`() = runTest {
        repository.refreshResult = DataResult.Failure(DataError.NO_INTERNET)
        val viewModel = PostListViewModel(repository)

        repository.refreshResult = DataResult.Success(Unit)
        viewModel.onAction(PostListAction.Retry)

        assertNull(viewModel.uiState.value.syncError)
        assertEquals(2, repository.refreshCalls)
    }

    @Test
    fun `successful delete keeps the post marked as deleting`() = runTest {
        val viewModel = PostListViewModel(repository)

        viewModel.onAction(PostListAction.Delete(7))

        assertEquals(listOf(7), repository.deleteCalls)
        assertEquals(setOf(7), viewModel.uiState.value.deletingIds)
    }

    @Test
    fun `failed delete clears the deleting state and shows a message`() = runTest {
        repository.deleteResult = DataResult.Failure(DataError.NO_INTERNET)
        val viewModel = PostListViewModel(repository)

        viewModel.events.test {
            viewModel.onAction(PostListAction.Delete(7))

            assertEquals(
                PostListEvent.ShowMessage(UiText.StringResource(R.string.error_delete_failed)),
                awaitItem(),
            )
        }
        assertTrue(viewModel.uiState.value.deletingIds.isEmpty())
    }

    @Test
    fun `search query is debounced before reaching the repository`() = runTest {
        val viewModel = PostListViewModel(repository)
        backgroundScope.launch { viewModel.posts.collect {} }
        runCurrent()

        viewModel.onAction(PostListAction.QueryChange("q"))
        viewModel.onAction(PostListAction.QueryChange("qui"))
        advanceTimeBy(301)

        assertEquals(listOf(null, "qui"), repository.pagedQueries)
    }
}
