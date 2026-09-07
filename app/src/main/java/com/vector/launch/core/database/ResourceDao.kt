package com.vector.launch.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY createdAt DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getResourcesForProject(projectId: String): Flow<List<ResourceEntity>>

    @Query("UPDATE resources SET projectId = NULL WHERE projectId = :projectId")
    fun detachResourcesFromProject(projectId: String)

    @Query("DELETE FROM resources WHERE projectId = :projectId")
    fun deleteResourcesForProject(projectId: String)

    @Query("SELECT * FROM resources WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getResourcesForTask(taskId: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE id = :id")
    fun getResourceById(id: String): ResourceEntity?

    @Query("SELECT * FROM resources ORDER BY createdAt ASC")
    fun getAllResourcesSnapshot(): List<ResourceEntity>

    @Query("SELECT * FROM resources WHERE title LIKE '%' || :query || '%' ESCAPE '\\' OR uri LIKE '%' || :query || '%' ESCAPE '\\'")
    fun searchResources(query: String): List<ResourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertResource(resource: ResourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertResources(resources: List<ResourceEntity>)

    @Update
    fun updateResource(resource: ResourceEntity)

    @Delete
    fun deleteResource(resource: ResourceEntity)

    @Query("DELETE FROM resources")
    fun deleteAllResources()
}
