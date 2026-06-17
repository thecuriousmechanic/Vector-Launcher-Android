package com.vector.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProjectWorkspace(
    projectName: String,
    onBack: () -> Unit
) {

    val tasks = remember {

        mutableStateListOf(

            Task("Continue Development"),
            Task("Review Notes"),
            Task("Plan Next Sprint")
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
            text = projectName,
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

        KnowledgeCard(
            title = "Design Notes"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        KnowledgeCard(
            title = "Research"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        KnowledgeCard(
            title = "Ideas"
        )
    }
}