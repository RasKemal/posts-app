package com.example.postsapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.postsapplication.ui.common.shimmer

/** Post image with a shimmer while loading; on error the placeholder color stays. */
@Composable
fun PostImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    var isLoading by remember(url) { mutableStateOf(true) }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty
        },
        modifier = modifier.then(
            if (isLoading) Modifier.shimmer(placeholderColor) else Modifier.background(placeholderColor),
        ),
    )
}
