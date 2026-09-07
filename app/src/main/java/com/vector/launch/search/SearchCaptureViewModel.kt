package com.vector.launch.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.SearchResult
import com.vector.launch.core.model.SearchResultType
import com.vector.launch.core.model.Task
import com.vector.launch.core.repository.NoteRepository
import com.vector.launch.core.repository.SearchRepository
import com.vector.launch.core.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchCaptureUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val selectedFilter: SearchResultType? = null,
    val quickCaptureInput: String = "",
    val isCapturingNote: Boolean = false,
    val isSuccessMessage: String? = null
)

class SearchCaptureViewModel(
    private val searchRepository: SearchRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchCaptureUiState())
    val uiState: StateFlow<SearchCaptureUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun updateQuery(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        scheduleSearch(debounceMillis = 180L)
    }

    fun selectFilter(type: SearchResultType?) {
        val nextFilter = if (_uiState.value.selectedFilter == type) null else type
        _uiState.value = _uiState.value.copy(selectedFilter = nextFilter)
        scheduleSearch(debounceMillis = 0L)
    }

    private fun scheduleSearch(debounceMillis: Long) {
        searchJob?.cancel()

        val query = _uiState.value.query.trim()
        val filter = _uiState.value.selectedFilter
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            if (debounceMillis > 0) delay(debounceMillis)
            val results = searchRepository.search(query)
            val filtered = filter?.let { selected ->
                results.filter { it.type == selected }
            } ?: results

            val current = _uiState.value
            if (current.query.trim() == query && current.selectedFilter == filter) {
                _uiState.value = current.copy(results = filtered)
            }
        }
    }

    fun updateCaptureInput(input: String) {
        _uiState.value = _uiState.value.copy(quickCaptureInput = input)
    }

    fun toggleCaptureType() {
        _uiState.value = _uiState.value.copy(isCapturingNote = !_uiState.value.isCapturingNote)
    }

    fun submitQuickCapture() {
        val state = _uiState.value
        val input = state.quickCaptureInput.trim()
        if (input.isBlank()) return

        _uiState.value = state.copy(quickCaptureInput = "")

        viewModelScope.launch {
            if (state.isCapturingNote) {
                val note = Note(
                    title = input.take(40),
                    content = input
                )
                noteRepository.saveNote(note)
                _uiState.value = _uiState.value.copy(
                    isSuccessMessage = "Captured Note: '${note.title}'"
                )
            } else {
                val task = Task(
                    title = input
                )
                taskRepository.saveTask(task)
                _uiState.value = _uiState.value.copy(
                    isSuccessMessage = "Captured Task: '${task.title}'"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(isSuccessMessage = null)
    }
}
