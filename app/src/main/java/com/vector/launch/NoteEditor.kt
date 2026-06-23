package com.vector.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NoteEditor(
    note: Note,
    onBack: () -> Unit
) {

    var content by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    LaunchedEffect(note.title) {

        content =
            NoteStorage.loadNote(
                context,
                note.title
            )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "← Back",
            color = Color.Gray,
            modifier = Modifier.clickable {
                onBack()
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = note.title,
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HorizontalDivider(
            color = Color.DarkGray
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        BasicTextField(
            value = content,
            onValueChange = {

                content = it

                CoroutineScope(
                    Dispatchers.IO
                ).launch {

                    NoteStorage.saveNote(
                        context,
                        note.title,
                        it
                    )
                }
            },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}