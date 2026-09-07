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
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY createdAt ASC")
    fun getAllProjectsSnapshot(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' ESCAPE '\\' OR description LIKE '%' || :query || '%' ESCAPE '\\'")
    fun searchProjects(query: String): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProjects(projects: List<ProjectEntity>)

    @Update
    fun updateProject(project: ProjectEntity)

    @Delete
    fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    fun deleteProjectById(id: String)

    @Query("DELETE FROM projects")
    fun deleteAllProjects()

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    fun deleteTasksForProject(projectId: String)

    @Query("DELETE FROM notes WHERE projectId = :projectId")
    fun deleteNotesForProject(projectId: String)

    @Query("DELETE FROM resources WHERE projectId = :projectId")
    fun deleteResourcesForProject(projectId: String)

    @Transaction
    fun deleteProjectWithRelations(projectId: String) {
        deleteResourcesForProject(projectId)
        deleteNotesForProject(projectId)
        deleteTasksForProject(projectId)
        deleteProjectById(projectId)
    }
}
