package com.vector.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ProjectWorkspace(
    project: ProjectData,
    onBack: () -> Unit
) {

    val tasks = remember(project.name) {
        mutableStateListOf(*project.tasks.toTypedArray())
    }

    var selectedNote by remember {
        mutableStateOf<Note?>(null)
    }

    val context = LocalContext.current

    LaunchedEffect(project.name) {

        tasks.indices.forEach { index ->

            val savedState =
                TaskStorage.loadTaskState(
                    context,
                    "${project.name}_${tasks[index].title}"
                )

            tasks[index] =
                tasks[index].copy(
                    completed = savedState
                )
        }
    }

    if (selectedNote != null) {

        NoteEditor(
            note = selectedNote!!,

            onBack = {
                selectedNote = null
            }
        )

        return
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
            text = project.name,
            color = Color.White,
            fontSize = 24.sp
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

        val activeTasks =
            tasks.filter { !it.completed }

        val completedTasks =
            tasks.filter { it.completed }

        activeTasks.forEach { task ->

            TaskCard(
                title = task.title,
                completed = false,

                onClick = {

                    val index =
                        tasks.indexOf(task)

                    tasks[index] =
                        task.copy(
                            completed = true
                        )

                    kotlinx.coroutines.CoroutineScope(
                        kotlinx.coroutines.Dispatchers.IO
                    ).launch {

                        TaskStorage.saveTaskState(
                            context,
                            "${project.name}_${task.title}",
                            true
                        )
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        if (completedTasks.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Completed",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            completedTasks.forEach { task ->

                TaskCard(
                    title = task.title,
                    completed = true,

                    onClick = {

                        val index =
                            tasks.indexOf(task)

                        tasks[index] =
                            task.copy(
                                completed = false
                            )

                        kotlinx.coroutines.CoroutineScope(
                            kotlinx.coroutines.Dispatchers.IO
                        ).launch {

                            TaskStorage.saveTaskState(
                                context,
                                "${project.name}_${task.title}",
                                false
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }
        }

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

        project.notes.forEach { note ->

            KnowledgeCard(
                title = note.title,

                onClick = {
                    selectedNote = note
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        }
    }
