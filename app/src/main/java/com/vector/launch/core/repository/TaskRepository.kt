package com.vector.launch.core.repository

import com.vector.launch.core.database.TaskDao
import com.vector.launch.core.database.TaskEntity
import com.vector.launch.core.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getGlobalTasks(): Flow<List<Task>> {
        return getActiveTasks()
    }

    fun getTasksForProject(projectId: String): Flow<List<Task>> {
        return taskDao.getTasksForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTaskById(id: String): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(id)?.toDomain()
    }

    suspend fun saveTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(TaskEntity.fromDomain(task))
    }

    suspend fun toggleTaskCompleted(task: Task): Task = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val newCompleted = !task.completed
        val updated = task.copy(
            completed = newCompleted,
            completedAt = if (newCompleted) now else null,
            updatedAt = now
        )
        taskDao.updateTask(TaskEntity.fromDomain(updated))
        updated
    }

    suspend fun toggleTaskToday(task: Task): Task = withContext(Dispatchers.IO) {
        if (task.projectId == null) return@withContext task
        val updated = task.copy(
            isToday = !task.isToday,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(TaskEntity.fromDomain(updated))
        updated
    }

    suspend fun deleteTask(id: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskWithResources(id)
    }
}
