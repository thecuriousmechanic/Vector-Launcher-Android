package com.vector.launch.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

const val FAVORITES_ITEM = '★'
val APP_ALPHABET = ('A'..'Z').toList() + '#'
val APP_NAVIGATION_ITEMS = listOf(FAVORITES_ITEM) + APP_ALPHABET

internal fun alphabetIndexForTouch(
    touchY: Float,
    height: Float,
    itemCount: Int
): Int? {
    if (height <= 0f || itemCount <= 0) return null
    val fraction = (touchY.coerceIn(0f, height) / height).coerceIn(0f, 1f)
    return (fraction * itemCount).toInt().coerceIn(0, itemCount - 1)
}

internal fun waveInfluence(distance: Float, sigma: Float): Float {
    if (sigma <= 0f) return 0f
    return exp(-(distance * distance) / (2f * sigma * sigma)).coerceIn(0f, 1f)
}

internal fun findNearestAlphabetSectionIndex(
    target: Char,
    sectionIndices: Map<Char, Int>,
    alphabet: List<Char> = APP_ALPHABET
): Int? {
    if (sectionIndices.isEmpty()) return null
    sectionIndices[target]?.let { return it }

    val targetPosition = alphabet.indexOf(target)
    if (targetPosition < 0) return sectionIndices.values.firstOrNull()

    for (position in targetPosition until alphabet.size) {
        sectionIndices[alphabet[position]]?.let { return it }
    }
    for (position in targetPosition - 1 downTo 0) {
        sectionIndices[alphabet[position]]?.let { return it }
    }
    return sectionIndices.values.firstOrNull()
}

@Composable
fun WaveAlphabetIndexBar(
    onScrubLetter: (Char) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<Char> = APP_NAVIGATION_ITEMS,
    availableItems: Set<Char> = items.toSet()
) {
    if (items.isEmpty()) return

    var barSize by remember { mutableStateOf(IntSize.Zero) }
    var isTouching by remember { mutableStateOf(false) }
    var rawTouchY by remember { mutableFloatStateOf(-1f) }
    var activeItem by remember { mutableStateOf<Char?>(null) }

    val currentOnScrubLetter by rememberUpdatedState(onScrubLetter)
    val currentOnScrubEnd by rememberUpdatedState(onScrubEnd)
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val railColor = MaterialTheme.colorScheme.onSurfaceVariant

    val engagement by animateFloatAsState(
        targetValue = if (isTouching) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "alphabetEngagement"
    )

    Box(
        modifier = modifier
            .width(76.dp)
            .fillMaxHeight()
            .onGloballyPositioned { barSize = it.size }
            .pointerInput(items) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isTouching = true
                    var lastIndex = -1

                    fun scrubAt(touchY: Float) {
                        val height = barSize.height.toFloat()
                        val index = alphabetIndexForTouch(touchY, height, items.size) ?: return
                        rawTouchY = touchY.coerceIn(0f, height)
                        if (index != lastIndex) {
                            lastIndex = index
                            activeItem = items[index]
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentOnScrubLetter(items[index])
                        }
                    }

                    try {
                        down.consume()
                        scrubAt(down.position.y)
                        drag(down.id) { change ->
                            change.consume()
                            scrubAt(change.position.y)
                        }
                    } finally {
                        isTouching = false
                        rawTouchY = -1f
                        activeItem = null
                        currentOnScrubEnd()
                    }
                }
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        val effectiveTouchY = rawTouchY.takeIf { it >= 0f && barSize.height > 0 }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val railX = size.width - 12.dp.toPx()
            drawLine(
                color = railColor.copy(alpha = 0.16f + engagement * 0.12f),
                start = Offset(railX, 10.dp.toPx()),
                end = Offset(railX, size.height - 10.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            if (effectiveTouchY != null) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.18f),
                    radius = 14.dp.toPx(),
                    center = Offset(railX, effectiveTouchY)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 3.dp.toPx(),
                    center = Offset(railX, effectiveTouchY)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 0.dp, top = 5.dp, end = 2.dp, bottom = 5.dp),
            horizontalAlignment = Alignment.End
        ) {
            val totalItems = items.size
            items.forEachIndexed { index, char ->
                val isAvailable = char in availableItems
                val isActive = char == activeItem
                val accessibilityDesc = when (char) {
                    FAVORITES_ITEM -> "Return to favorite apps"
                    '#' -> "Jump to numbers and symbols"
                    else -> "Jump to letter $char"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .semantics { contentDescription = accessibilityDesc }
                        .graphicsLayer {
                            if (effectiveTouchY != null && barSize.height > 0) {
                                val itemCenterY = (index + 0.5f) * (barSize.height.toFloat() / totalItems)
                                val distance = abs(itemCenterY - effectiveTouchY)
                                val influence = waveInfluence(distance, barSize.height.toFloat() / 9f) * engagement
                                val directionOffset = (effectiveTouchY - itemCenterY) * 0.1f * influence

                                translationX = -influence * 36.dp.toPx()
                                translationY = directionOffset
                                scaleX = 1f + influence * 0.68f
                                scaleY = 1f + influence * 0.68f
                                alpha = 0.48f + influence * 0.52f
                            } else {
                                translationX = 0f
                                translationY = 0f
                                scaleX = 1f
                                scaleY = 1f
                                alpha = if (isAvailable) 0.9f else 0.24f
                            }
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (isActive) primaryColor.copy(alpha = 0.18f) else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = if (char == FAVORITES_ITEM) 12.sp else 10.sp,
                            lineHeight = 11.sp,
                            fontWeight = if (isAvailable || isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAvailable || isActive) {
                                primaryColor
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isTouching && activeItem != null,
            enter = fadeIn() + scaleIn(initialScale = 0.82f),
            exit = fadeOut() + scaleOut(targetScale = 0.86f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val bubbleSize = with(density) { 52.dp.roundToPx() }
                    val x = with(density) { (-82).dp.roundToPx() }
                    val y = (rawTouchY.roundToInt() - bubbleSize / 2)
                        .coerceIn(0, (barSize.height - bubbleSize).coerceAtLeast(0))
                    IntOffset(x, y)
                }
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = activeItem?.toString().orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}
