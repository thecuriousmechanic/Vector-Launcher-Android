package com.vector.launch.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.Resource
import com.vector.launch.core.model.Task
import com.vector.launch.core.repository.NoteRepository
import com.vector.launch.core.repository.ProjectRepository
import com.vector.launch.core.repository.ResourceRepository
import com.vector.launch.core.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val noteRepository: NoteRepository,
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> = _resources.asStateFlow()

    private var currentTaskId: String? = null
    private var loadJob: Job? = null

    fun loadTask(taskId: String) {
        if (currentTaskId == taskId && loadJob?.isActive == true) return
        currentTaskId = taskId
        loadJob?.cancel()
        _task.value = null
        _project.value = null
        _notes.value = emptyList()
        _resources.value = emptyList()

        loadJob = viewModelScope.launch {
            val t = taskRepository.getTaskById(taskId)
            _task.value = t
            if (t != null) {
                if (t.projectId != null) {
                    _project.value = projectRepository.getProjectById(t.projectId)
                    _notes.value = noteRepository.getNotesForProject(t.projectId).firstOrNull() ?: emptyList()
                }
                _resources.value = resourceRepository.getResourcesForTask(t.id).firstOrNull() ?: emptyList()
            }
        }
    }

    fun toggleCompleted() {
        val current = _task.value ?: return
        viewModelScope.launch {
            _task.value = taskRepository.toggleTaskCompleted(current)
        }
    }

    fun toggleToday() {
        val current = _task.value ?: return
        viewModelScope.launch {
            _task.value = taskRepository.toggleTaskToday(current)
        }
    }

    fun deleteTask(onDeleted: () -> Unit) {
        val taskId = currentTaskId ?: return
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            onDeleted()
        }
    }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}
