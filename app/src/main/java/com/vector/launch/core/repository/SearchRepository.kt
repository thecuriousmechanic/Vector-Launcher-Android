package com.vector.launch.core.repository

import com.vector.launch.core.database.NoteDao
import com.vector.launch.core.database.ProjectDao
import com.vector.launch.core.database.ResourceDao
import com.vector.launch.core.database.TaskDao
import com.vector.launch.core.model.SearchResult
import com.vector.launch.core.model.SearchResultType

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal fun escapeSqlLikeQuery(query: String): String = buildString(query.length) {
    query.forEach { character ->
        if (character == '\\' || character == '%' || character == '_') append('\\')
        append(character)
    }
}

class SearchRepository(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
    private val noteDao: NoteDao,
    private val resourceDao: ResourceDao,
    private val appLauncherRepository: AppLauncherRepository? = null
) {
    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val cleanQuery = query.trim()
        val escapedQuery = escapeSqlLikeQuery(cleanQuery)
        val results = mutableListOf<SearchResult>()

        // Search Installed Apps
        appLauncherRepository?.searchApps(cleanQuery)?.forEach { app ->
            results.add(
                SearchResult(
                    id = "app-${app.packageName}",
                    type = SearchResultType.APPLICATION,
                    title = app.label,
                    subtitle = app.packageName,
                    packageName = app.packageName
                )
            )
        }

        // Search Tasks
        val tasks = taskDao.searchTasks(escapedQuery)
        tasks.forEach { task ->
            results.add(
                SearchResult(
                    id = task.id,
                    type = SearchResultType.TASK,
                    title = task.title,
                    subtitle = if (task.description.isNotBlank()) task.description else "Task",
                    projectId = task.projectId
                )
            )
        }

        // Search Projects
        val projects = projectDao.searchProjects(escapedQuery)
        projects.forEach { project ->
            results.add(
                SearchResult(
                    id = project.id,
                    type = SearchResultType.PROJECT,
                    title = project.name,
                    subtitle = if (project.description.isNotBlank()) project.description else "Project",
                    projectId = project.id
                )
            )
        }

        // Search Notes
        val notes = noteDao.searchNotes(escapedQuery)
        notes.forEach { note ->
            results.add(
                SearchResult(
                    id = note.id,
                    type = SearchResultType.NOTE,
                    title = note.title,
                    subtitle = if (note.content.isNotBlank()) note.content.take(60) else "Note",
                    projectId = note.projectId
                )
            )
        }

        // Search Resources
        val resources = resourceDao.searchResources(escapedQuery)
        resources.forEach { resource ->
            results.add(
                SearchResult(
                    id = resource.id,
                    type = SearchResultType.RESOURCE,
                    title = resource.title,
                    subtitle = resource.uri,
                    projectId = resource.projectId
                )
            )
        }

        results
    }
}
