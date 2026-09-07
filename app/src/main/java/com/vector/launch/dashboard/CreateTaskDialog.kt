package com.vector.launch.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.TaskPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    projects: List<Project>,
    initialProjectId: String? = null,
    onDismiss: () -> Unit,
    onCreateTask: (
        title: String,
        description: String,
        priority: TaskPriority,
        projectId: String?,
        isToday: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedProjectId by remember { mutableStateOf<String?>(initialProjectId) }
    var isToday by remember { mutableStateOf(initialProjectId == null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CREATE TASK",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                letterSpacing = 1.2.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (projects.isNotEmpty() && initialProjectId == null) {
                    Text(
                        text = "Project (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedProjectId == null,
                            onClick = {
                                selectedProjectId = null
                                isToday = true
                            },
                            label = { Text("None") }
                        )
                        projects.take(3).forEach { project ->
                            FilterChip(
                                selected = selectedProjectId == project.id,
                                onClick = {
                                    selectedProjectId = project.id
                                    isToday = false
                                },
                                label = { Text(project.name.take(12)) }
                            )
                        }
                    }
                }

                if (selectedProjectId != null) {
                    FilterChip(
                        selected = isToday,
                        onClick = { isToday = !isToday },
                        label = { Text("Show in Today") }
                    )
                    Text(
                        text = "Project tasks stay in their project unless you place them on Today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TaskPriority.HIGH to "High",
                        TaskPriority.MEDIUM to "Normal",
                        TaskPriority.LOW to "Low"
                    ).forEach { (option, label) ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateTask(title.trim(), description.trim(), priority, selectedProjectId, isToday)
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
