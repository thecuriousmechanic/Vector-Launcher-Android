package com.vector.launch.projects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.launch.core.model.TaskPriority
import com.vector.launch.core.ui.EmptyStateView
import com.vector.launch.core.ui.openExternalUri
import com.vector.launch.core.ui.TaskRow
import com.vector.launch.core.ui.VectorCard
import com.vector.launch.dashboard.CreateTaskDialog

@Composable
fun ProjectDetailScreen(
    viewModel: ProjectDetailViewModel,
    projectId: String,
    onBack: () -> Unit,
    onNavigateToTask: (String) -> Unit,
    onNavigateToNote: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project = uiState.project

    if (uiState.isEditProjectOpen && project != null) {
        EditProjectDialog(
            initialName = project.name,
            initialDescription = project.description,
            onDismiss = { viewModel.closeEditProjectDialog() },
            onConfirm = { name, desc -> viewModel.updateProject(name, desc) }
        )
    }

    if (uiState.isDeleteConfirmationOpen && project != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteConfirmation() },
            title = { Text("Delete Project", fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${project.name}\"? This will permanently delete the project, its tasks, its notes, and any exclusively owned resources.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteProject(onDeleted = onBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteConfirmation() }) {
                    Text("CANCEL")
                }
            }
        )
    }

    uiState.pendingItemDeletion?.let { target ->
        val itemType = when (target) {
            is ProjectItemDeletion.TaskItem -> "task"
            is ProjectItemDeletion.NoteItem -> "note"
            is ProjectItemDeletion.ResourceItem -> "resource"
        }
        AlertDialog(
            onDismissRequest = viewModel::dismissItemDeletion,
            title = { Text("Delete $itemType?") },
            text = { Text("“${target.label}” will be permanently removed from this project.") },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmItemDeletion,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissItemDeletion) { Text("Cancel") }
            }
        )
    }

    if (uiState.isCreateTaskOpen) {
        CreateTaskDialog(
            projects = emptyList(),
            initialProjectId = uiState.project?.id,
            onDismiss = { viewModel.closeCreateTaskDialog() },
            onCreateTask = { title, desc, priority, _, isToday ->
                viewModel.createTaskForProject(title, desc, priority, isToday)
            }
        )
    }

    if (uiState.isCreateResourceOpen) {
        CreateResourceDialog(
            onDismiss = viewModel::closeCreateResourceDialog,
            onCreate = viewModel::createResourceForProject
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Project",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (project != null) {
                Row {
                    IconButton(onClick = { viewModel.openEditProjectDialog() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Project", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.openDeleteConfirmation() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Project", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (project == null) {
            EmptyStateView(message = "Project not found.")
        } else {
            // Project Summary Banner
            VectorCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ) {
                Column {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (project.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress: ${(uiState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.tasks.count { it.completed }}/${uiState.tasks.size} Tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Tasks Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { viewModel.openCreateTaskDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Task", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (uiState.tasks.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "No tasks in this project.",
                            actionText = "+ Add Task",
                            onAction = { viewModel.openCreateTaskDialog() }
                        )
                    }
                } else {
                    val sortedProjectTasks = uiState.tasks.let { list ->
                        val incomplete = list.filter { !it.completed }
                        val completed = list.filter { it.completed }.sortedBy { it.completedAt ?: Long.MAX_VALUE }
                        incomplete + completed
                    }
                    items(sortedProjectTasks, key = { it.id }) { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TaskRow(
                                task = task,
                                onToggleTask = viewModel::toggleTask,
                                onSelectTask = onNavigateToTask,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.requestTaskDeletion(task) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete ${task.title}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Notes Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            viewModel.createNoteForProject { noteId ->
                                onNavigateToNote(noteId)
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Note", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Note", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (uiState.notes.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "No notes attached to this project.",
                            actionText = "+ Add Note",
                            onAction = {
                                viewModel.createNoteForProject { noteId ->
                                    onNavigateToNote(noteId)
                                }
                            }
                        )
                    }
                } else {
                    items(uiState.notes, key = { it.id }) { note ->
                        VectorCard(
                            padding = 12.dp,
                            onClick = { onNavigateToNote(note.id) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Note",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (note.content.isNotBlank()) {
                                        Text(
                                            text = note.content.take(60),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.requestNoteDeletion(note) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete ${note.title}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = viewModel::openCreateResourceDialog) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add link")
                        }
                    }
                }

                if (uiState.resources.isEmpty()) {
                    item {
                        Text(
                            text = "No links saved for this project.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(uiState.resources, key = { it.id }) { resource ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VectorCard(
                                onClick = { openExternalUri(context, resource.uri) },
                                padding = 12.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = resource.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = resource.uri,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.requestResourceDeletion(resource) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete ${resource.title}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateResourceDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, uri: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add project link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uri,
                    onValueChange = { uri = it },
                    label = { Text("Web address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title.trim(), uri.trim()) },
                enabled = title.isNotBlank() && uri.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditProjectDialog(
    initialName: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit project",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), description.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
