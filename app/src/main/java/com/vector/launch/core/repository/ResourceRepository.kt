package com.vector.launch.core.repository

import com.vector.launch.core.database.ResourceDao
import com.vector.launch.core.database.ResourceEntity
import com.vector.launch.core.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ResourceRepository(private val resourceDao: ResourceDao) {

    fun getAllResources(): Flow<List<Resource>> {
        return resourceDao.getAllResources().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getResourcesForProject(projectId: String): Flow<List<Resource>> {
        return resourceDao.getResourcesForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getResourcesForTask(taskId: String): Flow<List<Resource>> {
        return resourceDao.getResourcesForTask(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveResource(resource: Resource) = withContext(Dispatchers.IO) {
        resourceDao.insertResource(ResourceEntity.fromDomain(resource))
    }

    suspend fun deleteResource(resource: Resource) = withContext(Dispatchers.IO) {
        resourceDao.deleteResource(ResourceEntity.fromDomain(resource))
    }
}
