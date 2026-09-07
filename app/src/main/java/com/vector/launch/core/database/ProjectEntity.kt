package com.vector.launch.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vector.launch.core.model.Project

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Project = Project(
        id = id,
        name = name,
        description = description,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(project: Project): ProjectEntity = ProjectEntity(
            id = project.id,
            name = project.name,
            description = project.description,
            colorHex = project.colorHex,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }
}
