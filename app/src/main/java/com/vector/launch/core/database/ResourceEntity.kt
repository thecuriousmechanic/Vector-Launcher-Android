package com.vector.launch.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vector.launch.core.model.Resource
import com.vector.launch.core.model.ResourceType

@Entity(
    tableName = "resources",
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["taskId"])
    ]
)
data class ResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val uri: String,
    val type: String,
    val projectId: String?,
    val taskId: String?,
    val createdAt: Long
) {
    fun toDomain(): Resource = Resource(
        id = id,
        title = title,
        uri = uri,
        type = try { ResourceType.valueOf(type) } catch (e: Exception) { ResourceType.LINK },
        projectId = projectId,
        taskId = taskId,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(resource: Resource): ResourceEntity = ResourceEntity(
            id = resource.id,
            title = resource.title,
            uri = resource.uri,
            type = resource.type.name,
            projectId = resource.projectId,
            taskId = resource.taskId,
            createdAt = resource.createdAt
        )
    }
}
