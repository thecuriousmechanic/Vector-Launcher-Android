package com.vector.launch.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.Resource
import com.vector.launch.core.model.Task
import com.vector.launch.core.model.TaskPriority
import com.vector.launch.core.repository.NoteRepository
import com.vector.launch.core.repository.ProjectRepository
import com.vector.launch.core.repository.ResourceRepository
import com.vector.launch.core.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface ProjectItemDeletion {
    val label: String

    data class TaskItem(val task: Task) : ProjectItemDeletion {
        override val label: String = task.title
    }

    data class NoteItem(val note: Note) : ProjectItemDeletion {
        override val label: String = note.title.ifBlank { "Untitled note" }
    }

    data class ResourceItem(val resource: Resource) : ProjectItemDeletion {
        override val label: String = resource.title
    }
}

internal fun normalizeResourceUri(uri: String): String {
    val trimmed = uri.trim()
    return if ("://" in trimmed || trimmed.startsWith("content:") || trimmed.startsWith("file:")) {
        trimmed
    } else {
        "https://$trimmed"
    }
}

data class ProjectDetailUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
    val notes: List<Note> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val progress: Float = 0f,
    val isEditProjectOpen: Boolean = false,
    val isDeleteConfirmationOpen: Boolean = false,
    val isCreateTaskOpen: Boolean = false,
    val isCreateResourceOpen: Boolean = false,
    val pendingItemDeletion: ProjectItemDeletion? = null
)

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private var currentProjectId: String? = null
    private var loadJob: Job? = null

    fun loadProject(projectId: String) {
        if (currentProjectId == projectId && loadJob?.isActive == true) return
        currentProjectId = projectId
        loadJob?.cancel()
        _uiState.value = ProjectDetailUiState()
        loadJob = viewModelScope.launch {
            val proj = projectRepository.getProjectById(projectId)
            if (proj != null) {
                _uiState.value = _uiState.value.copy(project = proj)
                combine(
                    taskRepository.getTasksForProject(projectId),
                    noteRepository.getNotesForProject(projectId),
                    resourceRepository.getResourcesForProject(projectId)
                ) { tasks, notes, resources ->
                    val completedCount = tasks.count { it.completed }
                    val totalCount = tasks.size
                    val calcProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                    ProjectDetailUiState(
                        tasks = tasks,
                        notes = notes,
                        resources = resources,
                        progress = calcProgress
                    )
                }.collect { state ->
                    _uiState.value = _uiState.value.copy(
                        tasks = state.tasks,
                        notes = state.notes,
                        resources = state.resources,
                        progress = state.progress
                    )
                }
            }
        }
    }

    fun openEditProjectDialog() {
        _uiState.value = _uiState.value.copy(isEditProjectOpen = true)
    }

    fun closeEditProjectDialog() {
        _uiState.value = _uiState.value.copy(isEditProjectOpen = false)
    }

    fun updateProject(name: String, description: String) {
        val current = _uiState.value.project ?: return
        viewModelScope.launch {
            val updated = current.copy(name = name, description = description, updatedAt = System.currentTimeMillis())
            projectRepository.saveProject(updated)
            _uiState.value = _uiState.value.copy(project = updated, isEditProjectOpen = false)
        }
    }

    fun openDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(isDeleteConfirmationOpen = true)
    }

    fun closeDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(isDeleteConfirmationOpen = false)
    }

    fun deleteProject(onDeleted: () -> Unit) {
        val projId = currentProjectId ?: return
        viewModelScope.launch {
            projectRepository.deleteProjectById(projId)
            _uiState.value = _uiState.value.copy(isDeleteConfirmationOpen = false)
            onDeleted()
        }
    }

    fun openCreateTaskDialog() {
        _uiState.value = _uiState.value.copy(isCreateTaskOpen = true)
    }

    fun closeCreateTaskDialog() {
        _uiState.value = _uiState.value.copy(isCreateTaskOpen = false)
    }

    fun createTaskForProject(
        title: String,
        description: String,
        priority: TaskPriority,
        isToday: Boolean
    ) {
        val projId = currentProjectId ?: return
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority,
                projectId = projId,
                isToday = isToday
            )
            taskRepository.saveTask(task)
            _uiState.value = _uiState.value.copy(isCreateTaskOpen = false)
        }
    }

    fun createNoteForProject(onNoteCreated: (String) -> Unit) {
        val projId = currentProjectId ?: return
        viewModelScope.launch {
            val note = Note(
                title = "Untitled Note",
                content = "",
                projectId = projId
            )
            noteRepository.saveNote(note)
            onNoteCreated(note.id)
        }
    }

    fun openCreateResourceDialog() {
        _uiState.value = _uiState.value.copy(isCreateResourceOpen = true)
    }

    fun closeCreateResourceDialog() {
        _uiState.value = _uiState.value.copy(isCreateResourceOpen = false)
    }

    fun createResourceForProject(title: String, uri: String) {
        val projId = currentProjectId ?: return
        viewModelScope.launch {
            resourceRepository.saveResource(
                Resource(
                    title = title.trim(),
                    uri = normalizeResourceUri(uri),
                    projectId = projId
                )
            )
            _uiState.value = _uiState.value.copy(isCreateResourceOpen = false)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(task)
        }
    }

    fun requestTaskDeletion(task: Task) {
        _uiState.value = _uiState.value.copy(pendingItemDeletion = ProjectItemDeletion.TaskItem(task))
    }

    fun requestNoteDeletion(note: Note) {
        _uiState.value = _uiState.value.copy(pendingItemDeletion = ProjectItemDeletion.NoteItem(note))
    }

    fun requestResourceDeletion(resource: Resource) {
        _uiState.value = _uiState.value.copy(pendingItemDeletion = ProjectItemDeletion.ResourceItem(resource))
    }

    fun dismissItemDeletion() {
        _uiState.value = _uiState.value.copy(pendingItemDeletion = null)
    }

    fun confirmItemDeletion() {
        val target = _uiState.value.pendingItemDeletion ?: return
        _uiState.value = _uiState.value.copy(pendingItemDeletion = null)
        viewModelScope.launch {
            when (target) {
                is ProjectItemDeletion.TaskItem -> taskRepository.deleteTask(target.task.id)
                is ProjectItemDeletion.NoteItem -> noteRepository.deleteNote(target.note.id)
                is ProjectItemDeletion.ResourceItem -> resourceRepository.deleteResource(target.resource)
            }
        }
    }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}
