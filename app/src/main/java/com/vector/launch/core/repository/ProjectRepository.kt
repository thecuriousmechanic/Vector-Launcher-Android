package com.vector.launch.core.repository

import com.vector.launch.core.database.ProjectDao
import com.vector.launch.core.database.ProjectEntity
import com.vector.launch.core.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProjectRepository(private val projectDao: ProjectDao) {

    fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getProjectById(id: String): Project? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        projectDao.insertProject(ProjectEntity.fromDomain(project))
    }

    suspend fun deleteProjectById(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectWithRelations(projectId)
    }

    suspend fun deleteProject(project: Project) = withContext(Dispatchers.IO) {
        deleteProjectById(project.id)
    }
}
