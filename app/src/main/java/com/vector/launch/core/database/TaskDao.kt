package com.vector.launch.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isToday = 1 ORDER BY completed ASC, CASE WHEN completed = 0 THEN updatedAt END DESC, CASE WHEN completed = 1 THEN completedAt END ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isToday = 1 ORDER BY completed ASC, CASE WHEN completed = 0 THEN updatedAt END DESC, CASE WHEN completed = 1 THEN completedAt END ASC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET projectId = NULL WHERE projectId = :projectId")
    fun detachTasksFromProject(projectId: String)

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    fun deleteTasksForProject(projectId: String)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY completed ASC, CASE WHEN completed = 0 THEN updatedAt END DESC, CASE WHEN completed = 1 THEN completedAt END ASC")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY createdAt ASC")
    fun getAllTasksSnapshot(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' ESCAPE '\\' OR description LIKE '%' || :query || '%' ESCAPE '\\'")
    fun searchTasks(query: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(tasks: List<TaskEntity>)

    @Update
    fun updateTask(task: TaskEntity)

    @Delete
    fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks")
    fun deleteAllTasks()

    @Query("DELETE FROM resources WHERE taskId = :taskId")
    fun deleteResourcesForTask(taskId: String)

    @Transaction
    fun deleteTaskWithResources(taskId: String) {
        deleteResourcesForTask(taskId)
        deleteTaskById(taskId)
    }
}
