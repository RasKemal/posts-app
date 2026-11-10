package com.example.postsapplication.ui.screens.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.postsapplication.ui.common.shimmer
import com.example.postsapplication.ui.components.PostImage
import com.example.postsapplication.ui.screens.list.model.PostListItemUiModel
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
        }
    }
}

@Composable
fun PostRowPlaceholder(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PostRowHorizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(PostImageSize)
                .clip(CircleShape)
                .shimmer(color),
        )
        Spacer(Modifier.width(PostRowHorizontalPadding))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.6f).height(18.dp).clip(MaterialTheme.shapes.small).shimmer(color))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(MaterialTheme.shapes.small).shimmer(color))
            Box(Modifier.fillMaxWidth(0.8f).height(14.dp).clip(MaterialTheme.shapes.small).shimmer(color))
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
                listKey = "#1",
                title = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                bodyPreview = "quia et suscipit suscipit recusandae consequuntur expedita et cum reprehenderit",
                imageUrl = "",
            ),
            onClick = {},
        )
    }
}
