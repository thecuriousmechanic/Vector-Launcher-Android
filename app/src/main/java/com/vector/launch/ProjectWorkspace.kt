package com.vector.launch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProjectWorkspace(
    projectName: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = projectName,
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Vector Workspace",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HorizontalDivider(
            color = Color.DarkGray
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Tasks",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "• Continue Development",
            color = Color.White
        )

        Text(
            text = "• Review Notes",
            color = Color.White
        )

        Text(
            text = "• Plan Next Sprint",
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Knowledge",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Design Notes",
            color = Color.White
        )

        Text(
            text = "Research",
            color = Color.White
        )

        Text(
            text = "Ideas",
            color = Color.White
        )
    }
}