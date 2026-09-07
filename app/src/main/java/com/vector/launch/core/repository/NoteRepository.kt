package com.vector.launch.core.repository

import com.vector.launch.core.database.NoteDao
import com.vector.launch.core.database.NoteEntity
import com.vector.launch.core.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getGlobalNotes(): Flow<List<Note>> {
        return getAllNotes()
    }

    fun getNotesForProject(projectId: String): Flow<List<Note>> {
        return noteDao.getNotesForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)?.toDomain()
    }

    fun observeNoteById(id: String): Flow<Note?> {
        return noteDao.observeNoteById(id).map { it?.toDomain() }
    }

    suspend fun saveNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.insertNote(NoteEntity.fromDomain(note))
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }
}
