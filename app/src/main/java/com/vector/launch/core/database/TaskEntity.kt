package com.vector.launch.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.vector.launch.core.model.Task
import com.vector.launch.core.model.TaskPriority

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["projectId"]), Index(value = ["isToday"])]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val priority: String,
    val dueDate: Long?,
    val projectId: String?,
    @ColumnInfo(defaultValue = "0") val isToday: Boolean,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Task = Task(
        id = id,
        title = title,
        description = description,
        completed = completed,
        priority = try { TaskPriority.valueOf(priority) } catch (e: Exception) { TaskPriority.NONE },
        dueDate = dueDate,
        projectId = projectId,
        isToday = isToday,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            completed = task.completed,
            priority = task.priority.name,
            dueDate = task.dueDate,
            projectId = task.projectId,
            isToday = task.isToday,
            completedAt = task.completedAt,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
    }
}
