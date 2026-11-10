package com.example.postsapplication.ui.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.postsapplication.R
import com.example.postsapplication.domain.error.DataError
import com.example.postsapplication.ui.common.ObserveAsEvents
import com.example.postsapplication.ui.common.toErrorMessage
import com.example.postsapplication.ui.components.DelayedLoadingIndicator
import com.example.postsapplication.ui.components.EmptyState
import com.example.postsapplication.ui.components.EmptyStateAction
import com.example.postsapplication.ui.screens.list.components.PostImageSize
import com.example.postsapplication.ui.screens.list.components.PostRow
import com.example.postsapplication.ui.screens.list.components.PostRowHorizontalPadding
import com.example.postsapplication.ui.screens.list.components.PostRowPlaceholder
import com.example.postsapplication.ui.screens.list.components.SearchField
import com.example.postsapplication.ui.screens.list.components.SwipeToDeleteContainer
import com.example.postsapplication.ui.screens.list.model.PostListContent
import com.example.postsapplication.ui.screens.list.model.PostListItemUiModel
import com.example.postsapplication.ui.screens.list.model.postListContent
import com.example.postsapplication.ui.theme.PostsTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun PostListRoot(
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PostListEvent.SyncFailed -> if (posts.itemCount > 0) {
                snackbarHostState.showSnackbar(resources.getString(R.string.error_refresh_failed))
            }
        }
    }

    PostListScreen(
        uiState = uiState,
        posts = posts,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPostClick = onPostClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    uiState: PostListUiState,
    posts: LazyPagingItems<PostListItemUiModel>,
    snackbarHostState: SnackbarHostState,
    onAction: (PostListAction) -> Unit,
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_posts)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                SearchField(
                    query = uiState.query,
                    onQueryChange = { onAction(PostListAction.QueryChange(it)) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
        },
    ) { innerPadding ->
        val content = postListContent(
            itemCount = posts.itemCount,
            localRefresh = posts.loadState.refresh,
            query = uiState.query,
            isSyncing = uiState.isSyncing,
            syncError = uiState.syncError,
        )
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onAction(PostListAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            val bottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
            if (content == PostListContent.Posts) {
                PostList(
                    posts = posts,
                    query = uiState.query,
                    contentPadding = bottomPadding,
                    onPostClick = onPostClick,
                    onDelete = { onAction(PostListAction.Delete(it)) },
                )
            } else {
                PostListMessage(
                    content = content,
                    contentPadding = bottomPadding,
                    onRetry = { onAction(PostListAction.Retry) },
                )
            }
        }
    }
}

@Composable
private fun PostList(
    posts: LazyPagingItems<PostListItemUiModel>,
    query: String,
    contentPadding: PaddingValues,
    onPostClick: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    var scrolledForQuery by rememberSaveable { mutableStateOf(query) }
    LaunchedEffect(query) {
        if (query != scrolledForQuery) {
            scrolledForQuery = query
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding,
    ) {
        items(
            count = posts.itemCount,
            key = posts.itemKey { it.listKey },
            contentType = posts.itemContentType { "post" },
        ) { index ->
            val post = posts[index]
            if (post == null) {
                PostRowPlaceholder()
                return@items
            }
            Column(Modifier.animateItem()) {
                SwipeToDeleteContainer(
                    onDelete = { onDelete(post.id) },
                ) {
                    PostRow(post = post, onClick = { onPostClick(post.id) })
                }
                if (index < posts.itemCount - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = PostRowHorizontalPadding * 2 + PostImageSize),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

/** Loading, empty, no-results and error states. Scrollable so pull-to-refresh still works. */
@Composable
private fun PostListMessage(
    content: PostListContent,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                when (content) {
                    is PostListContent.Loading -> DelayedLoadingIndicator(
                        message = stringResource(
                            if (content.isSearching) R.string.loading_searching else R.string.loading_posts,
                        ),
                    )
                    PostListContent.Empty -> EmptyState(
                        iconRes = R.drawable.ic_posts_empty,
                        title = stringResource(R.string.empty_no_posts_title),
                        description = stringResource(R.string.empty_no_posts_description),
                    )
                    is PostListContent.NoResults -> EmptyState(
                        iconRes = R.drawable.ic_search_empty,
                        title = stringResource(R.string.empty_no_results_title),
                        description = stringResource(R.string.empty_no_results_description, content.query),
                    )
                    is PostListContent.Error -> {
                        val message = content.error.toErrorMessage()
                        EmptyState(
                            iconRes = R.drawable.ic_network_error,
                            title = message.title.asString(),
                            description = message.description.asString(),
                            iconTint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            action = EmptyStateAction(stringResource(R.string.action_retry), onRetry),
                        )
                    }
                    PostListContent.Posts -> Unit
                }
            }
        }
    }
}

private val previewPosts = (1..6).map { id ->
    PostListItemUiModel(
        id = id,
        listKey = "#$id",
        title = "sunt aut facere repellat provident occaecati excepturi optio $id",
        bodyPreview = "quia et suscipit suscipit recusandae consequuntur expedita et cum reprehenderit molestiae",
        imageUrl = "",
    )
}

private val notLoading = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)

@Composable
private fun PostListPreview(posts: List<PostListItemUiModel>, uiState: PostListUiState) {
    PostsTheme {
        PostListScreen(
            uiState = uiState,
            posts = flowOf(PagingData.from(posts, sourceLoadStates = notLoading)).collectAsLazyPagingItems(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onPostClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PostListPostsPreview() = PostListPreview(previewPosts, PostListUiState())

@Preview(showSystemUi = true)
@Composable
private fun PostListNoResultsPreview() = PostListPreview(emptyList(), PostListUiState(query = "xyz"))

@Preview(showSystemUi = true)
@Composable
private fun PostListErrorPreview() =
    PostListPreview(emptyList(), PostListUiState(syncError = DataError.NO_INTERNET))
