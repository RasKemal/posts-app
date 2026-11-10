package com.example.postsapplication.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.postsapplication.R
import com.example.postsapplication.ui.common.ObserveAsEvents
import com.example.postsapplication.ui.components.DelayedLoadingIndicator
import com.example.postsapplication.ui.components.EmptyState
import com.example.postsapplication.ui.components.EmptyStateAction
import com.example.postsapplication.ui.components.PostImage
import com.example.postsapplication.ui.theme.PostsTheme

@Composable
fun PostDetailRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PostDetailEvent.NavigateBack -> onBack()
            is PostDetailEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message.asString(resources))
        }
    }

    PostDetailScreen(
        uiState = uiState,
        titleState = viewModel.titleState,
        bodyState = viewModel.bodyState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    uiState: PostDetailUiState,
    titleState: TextFieldState,
    bodyState: TextFieldState,
    snackbarHostState: SnackbarHostState,
    onAction: (PostDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = uiState.hasChanges && !uiState.isSaving) {
        onAction(PostDetailAction.Back)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_edit_post)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(PostDetailAction.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        when {
            uiState.isLoading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                DelayedLoadingIndicator(message = stringResource(R.string.loading_post))
            }
            uiState.isNotFound -> Box(contentModifier, contentAlignment = Alignment.Center) {
                EmptyState(
                    iconRes = R.drawable.ic_search_empty,
                    title = stringResource(R.string.error_post_not_found),
                    action = EmptyStateAction(stringResource(R.string.cd_back)) { onAction(PostDetailAction.Back) },
                )
            }
            else -> PostEditForm(
                uiState = uiState,
                titleState = titleState,
                bodyState = bodyState,
                onSave = { onAction(PostDetailAction.Save) },
                modifier = contentModifier,
            )
        }
    }

    if (uiState.showDiscardDialog) {
        DiscardChangesDialog(
            onConfirm = { onAction(PostDetailAction.ConfirmDiscard) },
            onDismiss = { onAction(PostDetailAction.DismissDiscard) },
        )
    }
}

@Composable
private fun PostEditForm(
    uiState: PostDetailUiState,
    titleState: TextFieldState,
    bodyState: TextFieldState,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.imageUrl != null) {
            PostImage(
                url = uiState.imageUrl,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape),
            )
        }
        OutlinedTextField(
            state = titleState,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            label = { Text(stringResource(R.string.label_title)) },
            isError = uiState.isTitleBlank,
            supportingText = if (uiState.isTitleBlank) {
                { Text(stringResource(R.string.error_title_blank)) }
            } else {
                null
            },
            lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 3),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
        )
        OutlinedTextField(
            state = bodyState,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            label = { Text(stringResource(R.string.label_description)) },
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 5),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
        Button(
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun DiscardChangesDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_discard_title)) },
        text = { Text(stringResource(R.string.dialog_discard_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_discard)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_keep_editing)) }
        },
    )
}

@Composable
private fun PostDetailPreview(uiState: PostDetailUiState) {
    PostsTheme {
        PostDetailScreen(
            uiState = uiState,
            titleState = remember { TextFieldState(uiState.title) },
            bodyState = remember { TextFieldState(uiState.body) },
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

private val previewState = PostDetailUiState(
    isLoading = false,
    imageUrl = "",
    title = "qui est esse edited",
    body = "est rerum tempore vitae\nsequi sint nihil reprehenderit",
    originalTitle = "qui est esse",
    originalBody = "est rerum tempore vitae\nsequi sint nihil reprehenderit",
)

@Preview(showSystemUi = true)
@Composable
private fun PostDetailEditingPreview() = PostDetailPreview(previewState)

@Preview(showSystemUi = true)
@Composable
private fun PostDetailBlankTitlePreview() = PostDetailPreview(previewState.copy(title = ""))

@Preview(showSystemUi = true)
@Composable
private fun PostDetailNotFoundPreview() = PostDetailPreview(
    PostDetailUiState(isLoading = false, isNotFound = true),
)
