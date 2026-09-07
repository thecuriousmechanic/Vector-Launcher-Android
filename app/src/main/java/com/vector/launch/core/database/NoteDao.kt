package com.vector.launch.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE projectId IS NULL ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getNotesForProject(projectId: String): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET projectId = NULL WHERE projectId = :projectId")
    fun detachNotesFromProject(projectId: String)

    @Query("DELETE FROM notes WHERE projectId = :projectId")
    fun deleteNotesForProject(projectId: String)

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY createdAt ASC")
    fun getAllNotesSnapshot(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeNoteById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' ESCAPE '\\' OR content LIKE '%' || :query || '%' ESCAPE '\\'")
    fun searchNotes(query: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotes(notes: List<NoteEntity>)

    @Update
    fun updateNote(note: NoteEntity)

    @Delete
    fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteNoteById(id: String)

    @Query("DELETE FROM notes")
    fun deleteAllNotes()
}
