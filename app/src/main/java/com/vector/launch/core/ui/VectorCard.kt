package com.vector.launch.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VectorCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = Color.Transparent,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 20.dp,
    padding: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    var boxModifier = modifier
        .clip(shape)
        .background(backgroundColor, shape)

    if (borderColor != Color.Transparent) {
        boxModifier = boxModifier.border(BorderStroke(1.dp, borderColor), shape)
    }

    if (onClick != null) {
        boxModifier = boxModifier.clickable(onClick = onClick)
    }

    Box(
        modifier = boxModifier.padding(padding),
        content = content
    )
}
