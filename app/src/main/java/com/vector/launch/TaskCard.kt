package com.vector.launch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun TaskCard(
    title: String,
    completed: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),

        border = BorderStroke(
            1.dp,
            Color.DarkGray
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "[$title]",

                color =
                    if (completed)
                        Color.Gray
                    else
                        Color.White,

                textDecoration =
                    if (completed)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None
            )
        }
    }
}