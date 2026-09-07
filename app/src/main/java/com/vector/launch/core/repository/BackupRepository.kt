package com.vector.launch.core.repository

import androidx.room.withTransaction
import com.vector.launch.core.database.NoteEntity
import com.vector.launch.core.database.ProjectEntity
import com.vector.launch.core.database.ResourceEntity
import com.vector.launch.core.database.TaskEntity
import com.vector.launch.core.database.VectorDatabase
import com.vector.launch.core.datastore.PreferencesRepository
import com.vector.launch.core.datastore.PreferencesSnapshot
import com.vector.launch.core.model.DashboardWidgetConfig
import com.vector.launch.core.model.WidgetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(
    val taskCount: Int,
    val projectCount: Int,
    val noteCount: Int,
    val resourceCount: Int
) {
    val totalItemCount: Int get() = taskCount + projectCount + noteCount + resourceCount
}

private data class ParsedBackup(
    val tasks: List<TaskEntity>,
    val projects: List<ProjectEntity>,
    val notes: List<NoteEntity>,
    val resources: List<ResourceEntity>,
    val preferences: PreferencesSnapshot
)

class BackupRepository(
    private val database: VectorDatabase,
    private val preferencesRepository: PreferencesRepository
) {
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val preferences = preferencesRepository.createSnapshot()
        JSONObject().apply {
            put("format", BACKUP_FORMAT)
            put("version", BACKUP_VERSION)
            put("createdAt", System.currentTimeMillis())
            put("projects", database.projectDao().getAllProjectsSnapshot().toJsonArray(::projectToJson))
            put("tasks", database.taskDao().getAllTasksSnapshot().toJsonArray(::taskToJson))
            put("notes", database.noteDao().getAllNotesSnapshot().toJsonArray(::noteToJson))
            put("resources", database.resourceDao().getAllResourcesSnapshot().toJsonArray(::resourceToJson))
            put("preferences", preferencesToJson(preferences))
        }.toString(2)
    }

    suspend fun restoreBackupJson(json: String): BackupSummary = withContext(Dispatchers.IO) {
        val backup = parseAndValidateBackup(json)
        database.withTransaction {
            database.resourceDao().deleteAllResources()
            database.noteDao().deleteAllNotes()
            database.taskDao().deleteAllTasks()
            database.projectDao().deleteAllProjects()

            database.projectDao().insertProjects(backup.projects)
            database.taskDao().insertTasks(backup.tasks)
            database.noteDao().insertNotes(backup.notes)
            database.resourceDao().insertResources(backup.resources)
        }
        preferencesRepository.restoreSnapshot(backup.preferences)
        BackupSummary(
            taskCount = backup.tasks.size,
            projectCount = backup.projects.size,
            noteCount = backup.notes.size,
            resourceCount = backup.resources.size
        )
    }

    private fun parseAndValidateBackup(json: String): ParsedBackup {
        val root = JSONObject(json)
        require(root.optString("format") == BACKUP_FORMAT) { "This is not a Vector Launcher backup." }
        require(root.optInt("version", -1) == BACKUP_VERSION) { "This backup version is not supported." }

        val projects = root.requiredArray("projects").mapObjects(::parseProject)
        val tasks = root.requiredArray("tasks").mapObjects(::parseTask)
        val notes = root.requiredArray("notes").mapObjects(::parseNote)
        val resources = root.requiredArray("resources").mapObjects(::parseResource)
        val preferences = parsePreferences(root.getJSONObject("preferences"))

        requireUniqueIds(projects.map(ProjectEntity::id), "project")
        requireUniqueIds(tasks.map(TaskEntity::id), "task")
        requireUniqueIds(notes.map(NoteEntity::id), "note")
        requireUniqueIds(resources.map(ResourceEntity::id), "resource")

        val projectIds = projects.mapTo(hashSetOf(), ProjectEntity::id)
        val taskIds = tasks.mapTo(hashSetOf(), TaskEntity::id)
        require(tasks.all { it.projectId == null || it.projectId in projectIds }) {
            "A task refers to a project that is missing from the backup."
        }
        require(notes.all { it.projectId == null || it.projectId in projectIds }) {
            "A note refers to a project that is missing from the backup."
        }
        require(resources.all { resource ->
            (resource.projectId == null || resource.projectId in projectIds) &&
                (resource.taskId == null || resource.taskId in taskIds)
        }) { "A resource refers to missing project or task data." }

        return ParsedBackup(tasks, projects, notes, resources, preferences)
    }

    private fun parseProject(obj: JSONObject) = ProjectEntity(
        id = obj.requiredString("id"),
        name = obj.requiredString("name"),
        description = obj.optString("description"),
        colorHex = obj.optString("colorHex", "#6366F1"),
        createdAt = obj.getLong("createdAt"),
        updatedAt = obj.getLong("updatedAt")
    )

    private fun parseTask(obj: JSONObject) = TaskEntity(
        id = obj.requiredString("id"),
        title = obj.requiredString("title"),
        description = obj.optString("description"),
        completed = obj.optBoolean("completed"),
        priority = obj.optString("priority", "NONE"),
        dueDate = obj.nullableLong("dueDate"),
        projectId = obj.nullableString("projectId"),
        isToday = obj.isNull("projectId") || obj.optBoolean("isToday"),
        completedAt = obj.nullableLong("completedAt"),
        createdAt = obj.getLong("createdAt"),
        updatedAt = obj.getLong("updatedAt")
    )

    private fun parseNote(obj: JSONObject) = NoteEntity(
        id = obj.requiredString("id"),
        title = obj.requiredString("title"),
        content = obj.optString("content"),
        projectId = obj.nullableString("projectId"),
        createdAt = obj.getLong("createdAt"),
        updatedAt = obj.getLong("updatedAt")
    )

    private fun parseResource(obj: JSONObject) = ResourceEntity(
        id = obj.requiredString("id"),
        title = obj.requiredString("title"),
        uri = obj.requiredString("uri"),
        type = obj.optString("type", "LINK"),
        projectId = obj.nullableString("projectId"),
        taskId = obj.nullableString("taskId"),
        createdAt = obj.getLong("createdAt")
    )

    private fun parsePreferences(obj: JSONObject): PreferencesSnapshot {
        val favorites = obj.optJSONArray("preferredAppPackages") ?: JSONArray()
        val defaultWidgets = PreferencesRepository.getDefaultWidgetConfigs()
        val widgets = obj.optJSONArray("homeSections")?.mapObjects { widget ->
            val type = runCatching {
                WidgetType.valueOf(widget.requiredString("type"))
            }.getOrElse { throw IllegalArgumentException("A Home section has an unsupported type.") }
            DashboardWidgetConfig(
                id = widget.requiredString("id"),
                type = type,
                title = widget.optString("title", type.name.lowercase().replaceFirstChar(Char::uppercase)),
                position = widget.optInt("position", 0),
                isVisible = widget.optBoolean("isVisible", true),
                isExpanded = widget.optBoolean("isExpanded", true)
            )
        } ?: defaultWidgets
        require(widgets.map(DashboardWidgetConfig::id).distinct().size == widgets.size) {
            "The backup contains duplicate Home section IDs."
        }
        require(widgets.map(DashboardWidgetConfig::type).containsAll(defaultWidgets.map(DashboardWidgetConfig::type))) {
            "The backup is missing a required Home section."
        }
        return PreferencesSnapshot(
            themeMode = obj.optString("themeMode", "OLED"),
            accentColorHex = obj.optString("accentColorHex", "#A5B4FC"),
            brightnessValue = obj.optDouble("brightnessValue", 1.0).toFloat(),
            contrastValue = obj.optDouble("contrastValue", 1.0).toFloat(),
            saturationValue = obj.optDouble("saturationValue", 1.0).toFloat(),
            wallpaperDimValue = obj.optDouble("wallpaperDimValue", 0.2).toFloat(),
            preferredAppPackages = List(favorites.length()) { favorites.getString(it) },
            widgetConfigs = widgets.sortedBy(DashboardWidgetConfig::position)
        )
    }

    private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray =
        JSONArray().also { array -> forEach { array.put(transform(it)) } }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { transform(getJSONObject(it)) }

    private fun projectToJson(project: ProjectEntity) = JSONObject().apply {
        put("id", project.id)
        put("name", project.name)
        put("description", project.description)
        put("colorHex", project.colorHex)
        put("createdAt", project.createdAt)
        put("updatedAt", project.updatedAt)
    }

    private fun taskToJson(task: TaskEntity) = JSONObject().apply {
        put("id", task.id)
        put("title", task.title)
        put("description", task.description)
        put("completed", task.completed)
        put("priority", task.priority)
        put("dueDate", task.dueDate ?: JSONObject.NULL)
        put("projectId", task.projectId ?: JSONObject.NULL)
        put("isToday", task.isToday)
        put("completedAt", task.completedAt ?: JSONObject.NULL)
        put("createdAt", task.createdAt)
        put("updatedAt", task.updatedAt)
    }

    private fun noteToJson(note: NoteEntity) = JSONObject().apply {
        put("id", note.id)
        put("title", note.title)
        put("content", note.content)
        put("projectId", note.projectId ?: JSONObject.NULL)
        put("createdAt", note.createdAt)
        put("updatedAt", note.updatedAt)
    }

    private fun resourceToJson(resource: ResourceEntity) = JSONObject().apply {
        put("id", resource.id)
        put("title", resource.title)
        put("uri", resource.uri)
        put("type", resource.type)
        put("projectId", resource.projectId ?: JSONObject.NULL)
        put("taskId", resource.taskId ?: JSONObject.NULL)
        put("createdAt", resource.createdAt)
    }

    private fun preferencesToJson(preferences: PreferencesSnapshot) = JSONObject().apply {
        put("themeMode", preferences.themeMode)
        put("accentColorHex", preferences.accentColorHex)
        put("brightnessValue", preferences.brightnessValue.toDouble())
        put("contrastValue", preferences.contrastValue.toDouble())
        put("saturationValue", preferences.saturationValue.toDouble())
        put("wallpaperDimValue", preferences.wallpaperDimValue.toDouble())
        put("preferredAppPackages", JSONArray(preferences.preferredAppPackages))
        put("homeSections", preferences.widgetConfigs.toJsonArray { config ->
            JSONObject().apply {
                put("id", config.id)
                put("type", config.type.name)
                put("title", config.title)
                put("position", config.position)
                put("isVisible", config.isVisible)
                put("isExpanded", config.isExpanded)
            }
        })
    }

    private fun JSONObject.requiredArray(key: String): JSONArray =
        optJSONArray(key) ?: error("Backup is missing $key data.")

    private fun JSONObject.requiredString(key: String): String =
        getString(key).trim().also { require(it.isNotEmpty()) { "$key cannot be empty." } }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key).takeIf(String::isNotBlank)

    private fun JSONObject.nullableLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else getLong(key)

    private fun requireUniqueIds(ids: List<String>, entityName: String) {
        require(ids.size == ids.toSet().size) { "The backup contains duplicate $entityName IDs." }
    }

    private companion object {
        const val BACKUP_FORMAT = "vector-launcher-backup"
        const val BACKUP_VERSION = 1
    }
}
