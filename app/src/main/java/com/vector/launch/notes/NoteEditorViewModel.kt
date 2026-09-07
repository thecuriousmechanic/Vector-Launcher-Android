package com.vector.launch.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.launch.core.model.Note
import com.vector.launch.core.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: String = "",
    val title: String = "",
    val content: String = "",
    val projectId: String? = null,
    val createdAt: Long = 0L,
    val isSaving: Boolean = false,
    val lastSavedTimestamp: Long = 0L
)

internal fun NoteEditorUiState.toNote(nowMillis: Long): Note = Note(
    id = noteId,
    title = title.ifBlank { "Untitled Note" },
    content = content,
    projectId = projectId,
    createdAt = createdAt.takeIf { it > 0L } ?: nowMillis,
    updatedAt = nowMillis
)

class NoteEditorViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private var loadJob: Job? = null

    fun loadNote(noteId: String) {
        if (_uiState.value.noteId == noteId) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            autoSaveJob?.cancelAndJoin()
            if (_uiState.value.isSaving) performSave()

            val note = noteRepository.getNoteById(noteId)
            if (note != null) {
                _uiState.value = NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    projectId = note.projectId,
                    createdAt = note.createdAt,
                    lastSavedTimestamp = note.updatedAt
                )
            } else {
                // New note
                val newNote = Note(id = noteId, title = "Untitled Note", content = "")
                _uiState.value = NoteEditorUiState(
                    noteId = newNote.id,
                    title = newNote.title,
                    content = newNote.content,
                    createdAt = newNote.createdAt,
                    lastSavedTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
        scheduleDebouncedSave()
    }

    fun updateContent(newContent: String) {
        _uiState.value = _uiState.value.copy(content = newContent)
        scheduleDebouncedSave()
    }

    private fun scheduleDebouncedSave() {
        _uiState.value = _uiState.value.copy(isSaving = true)
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(400) // 400ms debounce window
            performSave()
        }
    }

    private suspend fun performSave() {
        val state = _uiState.value
        if (state.noteId.isBlank()) return

        val now = System.currentTimeMillis()
        val updatedNote = state.toNote(now)
        noteRepository.saveNote(updatedNote)
        _uiState.value = _uiState.value.copy(
            isSaving = false,
            createdAt = updatedNote.createdAt,
            lastSavedTimestamp = now
        )
    }

    fun flushPendingChanges(onComplete: () -> Unit) {
        viewModelScope.launch {
            autoSaveJob?.cancelAndJoin()
            if (_uiState.value.isSaving) performSave()
            onComplete()
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        val noteId = _uiState.value.noteId.takeIf(String::isNotBlank) ?: return
        viewModelScope.launch {
            autoSaveJob?.cancelAndJoin()
            loadJob?.cancelAndJoin()
            noteRepository.deleteNote(noteId)
            _uiState.value = NoteEditorUiState()
            onDeleted()
        }
    }

    override fun onCleared() {
        autoSaveJob?.cancel()
        loadJob?.cancel()
        super.onCleared()
    }
}
