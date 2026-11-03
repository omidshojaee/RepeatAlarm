package com.omidshojaee.repeatalarm.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extension modifier that draws a clear, visible Material 3 scrollbar track and thumb
 * on the side of a scrollable container for [ScrollState].
 */
@Composable
fun Modifier.drawVerticalScrollbar(
    scrollState: ScrollState,
    width: Dp = 6.dp,
    paddingEnd: Dp = 4.dp,
    paddingTop: Dp = 8.dp,
    paddingBottom: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    minThumbHeight: Dp = 40.dp
): Modifier {
    val isScrollable = scrollState.maxValue > 0
    val alpha by animateFloatAsState(
        targetValue = if (isScrollable) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrollbar_alpha"
    )

    if (alpha <= 0f) return this

    return this.drawWithContent {
        drawContent()

        if (isScrollable) {
            val widthPx = width.toPx()
            val paddingEndPx = paddingEnd.toPx()
            val paddingTopPx = paddingTop.toPx()
            val paddingBottomPx = paddingBottom.toPx()
            val minThumbHeightPx = minThumbHeight.toPx()

            val viewportHeight = size.height - paddingTopPx - paddingBottomPx
            if (viewportHeight > 0) {
                val totalContentHeight = (scrollState.maxValue + scrollState.viewportSize).toFloat()
                val scrollRatio = scrollState.viewportSize.toFloat() / totalContentHeight

                val rawThumbHeight = viewportHeight * scrollRatio
                val thumbHeight = rawThumbHeight.coerceAtLeast(minThumbHeightPx).coerceAtMost(viewportHeight)

                val scrollFraction = (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
                val thumbOffset = scrollFraction * (viewportHeight - thumbHeight)

                val x = size.width - widthPx - paddingEndPx
                val y = paddingTopPx + thumbOffset

                val cornerRadius = CornerRadius(widthPx / 2f, widthPx / 2f)

                // Draw Track
                drawRoundRect(
                    color = trackColor.copy(alpha = trackColor.alpha * alpha),
                    topLeft = Offset(x, paddingTopPx),
                    size = Size(widthPx, viewportHeight),
                    cornerRadius = cornerRadius
                )

                // Draw Thumb
                drawRoundRect(
                    color = thumbColor.copy(alpha = thumbColor.alpha * alpha),
                    topLeft = Offset(x, y),
                    size = Size(widthPx, thumbHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

/**
 * Extension modifier that draws a clear, visible Material 3 scrollbar track and thumb
 * on the side of a scrollable container for [LazyListState].
 */
@Composable
fun Modifier.drawVerticalScrollbar(
    lazyListState: LazyListState,
    width: Dp = 6.dp,
    paddingEnd: Dp = 4.dp,
    paddingTop: Dp = 8.dp,
    paddingBottom: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    minThumbHeight: Dp = 40.dp
): Modifier {
    val layoutInfo = lazyListState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo

    val isScrollable = totalItems > 0 && visibleItems.isNotEmpty() &&
            (visibleItems.size < totalItems || visibleItems.first().offset < 0 || visibleItems.last().offset + visibleItems.last().size > layoutInfo.viewportEndOffset)

    val alpha by animateFloatAsState(
        targetValue = if (isScrollable) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrollbar_lazy_alpha"
    )

    if (alpha <= 0f) return this

    return this.drawWithContent {
        drawContent()

        if (isScrollable) {
            val widthPx = width.toPx()
            val paddingEndPx = paddingEnd.toPx()
            val paddingTopPx = paddingTop.toPx()
            val paddingBottomPx = paddingBottom.toPx()
            val minThumbHeightPx = minThumbHeight.toPx()

            val viewportHeight = size.height - paddingTopPx - paddingBottomPx
            if (viewportHeight > 0) {
                val firstVisibleItem = visibleItems.first()
                val avgItemHeight = visibleItems.sumOf { it.size }.toFloat() / visibleItems.size

                val estimatedTotalHeight = totalItems * avgItemHeight
                val scrollRatio = (visibleItems.size * avgItemHeight) / estimatedTotalHeight

                val rawThumbHeight = viewportHeight * scrollRatio
                val thumbHeight = rawThumbHeight.coerceAtLeast(minThumbHeightPx).coerceAtMost(viewportHeight)

                val scrolledItems = firstVisibleItem.index + (-firstVisibleItem.offset.toFloat() / firstVisibleItem.size.coerceAtLeast(1))
                val maxScrollItems = (totalItems - visibleItems.size).coerceAtLeast(1)
                val scrollFraction = (scrolledItems / maxScrollItems.toFloat()).coerceIn(0f, 1f)

                val thumbOffset = scrollFraction * (viewportHeight - thumbHeight)

                val x = size.width - widthPx - paddingEndPx
                val y = paddingTopPx + thumbOffset

                val cornerRadius = CornerRadius(widthPx / 2f, widthPx / 2f)

                // Draw Track
                drawRoundRect(
                    color = trackColor.copy(alpha = trackColor.alpha * alpha),
                    topLeft = Offset(x, paddingTopPx),
                    size = Size(widthPx, viewportHeight),
                    cornerRadius = cornerRadius
                )

                // Draw Thumb
                drawRoundRect(
                    color = thumbColor.copy(alpha = thumbColor.alpha * alpha),
                    topLeft = Offset(x, y),
                    size = Size(widthPx, thumbHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}
