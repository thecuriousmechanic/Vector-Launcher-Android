package com.vector.launch.core.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val completed: Boolean = false,
    val priority: TaskPriority = TaskPriority.NONE,
    val dueDate: Long? = null,
    val projectId: String? = null,
    val isToday: Boolean = projectId == null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
