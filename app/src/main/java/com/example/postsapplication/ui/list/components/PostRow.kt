package com.example.postsapplication.ui.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.postsapplication.R
import com.example.postsapplication.ui.components.PostImage
import com.example.postsapplication.ui.list.PostListItemUiModel
import com.example.postsapplication.ui.theme.PostsTheme

val PostImageSize = 56.dp
val PostRowHorizontalPadding = 16.dp

@Composable
fun PostRow(
    post: PostListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(horizontal = PostRowHorizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PostImage(
            url = post.imageUrl,
            modifier = Modifier
                .size(PostImageSize)
                .clip(CircleShape),
        )
        Spacer(Modifier.width(PostRowHorizontalPadding))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.bodyPreview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (post.isEdited) {
                Text(
                    text = stringResource(R.string.label_edited),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostRowPreview() {
    PostsTheme {
        PostRow(
            post = PostListItemUiModel(
                id = 1,
                title = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                bodyPreview = "quia et suscipit suscipit recusandae consequuntur expedita et cum reprehenderit",
                imageUrl = "",
                isEdited = true,
            ),
            onClick = {},
        )
    }
}
