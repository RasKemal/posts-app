package com.example.postsapplication.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.launch

private const val SHIMMER_DURATION_MILLIS = 1200

/** Loading shimmer built on [Modifier.Node] (only invalidates the draw phase). */
fun Modifier.shimmer(color: Color): Modifier = this then ShimmerElement(color)

private data class ShimmerElement(val color: Color) : ModifierNodeElement<ShimmerNode>() {
    override fun create() = ShimmerNode(color)
    override fun update(node: ShimmerNode) {
        node.color = color
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmer"
        properties["color"] = color
    }
}

private class ShimmerNode(var color: Color) : Modifier.Node(), DrawModifierNode {
    private val progress = Animatable(0f)

    override fun onAttach() {
        coroutineScope.launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(SHIMMER_DURATION_MILLIS, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            )
        }
    }

    override fun ContentDrawScope.draw() {
        val width = size.width
        val startX = -2 * width + 4 * width * progress.value
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(color, color.copy(alpha = 0.4f), color),
                start = Offset(startX, 0f),
                end = Offset(startX + width, size.height),
            ),
        )
        drawContent()
    }
}
