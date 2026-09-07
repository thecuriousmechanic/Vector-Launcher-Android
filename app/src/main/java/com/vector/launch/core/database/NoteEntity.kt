package com.vector.launch.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vector.launch.core.model.Note

@Entity(
    tableName = "notes",
    indices = [Index(value = ["projectId"])]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val projectId: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Note = Note(
        id = id,
        title = title,
        content = content,
        projectId = projectId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            projectId = note.projectId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
    }
}
