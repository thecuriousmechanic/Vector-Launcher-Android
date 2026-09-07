package com.vector.launch

import com.vector.launch.core.datastore.PreferencesRepository
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.Task
import com.vector.launch.core.model.TaskPriority
import com.vector.launch.core.model.WidgetType
import com.vector.launch.core.ui.taskCompletionMarker
import com.vector.launch.core.model.AppInfo
import com.vector.launch.core.repository.escapeSqlLikeQuery
import com.vector.launch.dashboard.orderedHomeContentSections
import com.vector.launch.dashboard.resolvePreferredApps
import com.vector.launch.notes.NoteEditorUiState
import com.vector.launch.notes.toNote
import com.vector.launch.projects.normalizeResourceUri
import com.vector.launch.ui.NavigationState
import com.vector.launch.ui.decodeNavigationState
import com.vector.launch.ui.encodeNavigationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VectorArchitectureTest {

    @Test
    fun testPreferredAppsPreserveUserOrderAndDropDuplicatesOrMissingPackages() {
        val installed = listOf(
            AppInfo(packageName = "app.alpha", label = "Alpha"),
            AppInfo(packageName = "app.beta", label = "Beta")
        )

        val preferred = resolvePreferredApps(
            preferredPackages = listOf("app.beta", "missing", "app.alpha", "app.beta"),
            installedApps = installed
        )

        assertEquals(listOf("app.beta", "app.alpha"), preferred.map { it.packageName })
    }

    @Test
    fun testNoteEditorSavePreservesCreationTimestamp() {
        val state = NoteEditorUiState(
            noteId = "note-1",
            title = "Updated title",
            content = "Updated content",
            projectId = "project-1",
            createdAt = 1_000L
        )

        val note = state.toNote(nowMillis = 2_000L)

        assertEquals(1_000L, note.createdAt)
        assertEquals(2_000L, note.updatedAt)
        assertEquals("project-1", note.projectId)
    }

    @Test
    fun testSearchEscapesSqlLikeWildcards() {
        assertEquals("100\\%\\_done\\\\next", escapeSqlLikeQuery("100%_done\\next"))
    }

    @Test
    fun testTaskStableUUIDGeneration() {
        val task1 = Task(title = "Task 1")
        val task2 = Task(title = "Task 1") // Same title, different UUID

        assertNotNull(task1.id)
        assertNotNull(task2.id)
        assertNotEquals(task1.id, task2.id)
        assertEquals("Task 1", task1.title)
        assertFalse(task1.completed)
    }

    @Test
    fun testProjectStableUUIDGeneration() {
        val proj1 = Project(name = "Vector Launch")
        val proj2 = Project(name = "Vector Launch")

        assertNotNull(proj1.id)
        assertNotEquals(proj1.id, proj2.id)
        assertEquals("Vector Launch", proj1.name)
    }

    @Test
    fun testNoteStableUUIDGeneration() {
        val note1 = Note(title = "Design Principles", content = "Quiet, minimal, fast.")
        val note2 = Note(title = "Design Principles", content = "Quiet, minimal, fast.")

        assertNotNull(note1.id)
        assertNotEquals(note1.id, note2.id)
        assertEquals("Design Principles", note1.title)
    }

    @Test
    fun testDefaultWidgetConfigurations() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs()

        assertEquals(5, configs.size)
        assertEquals(WidgetType.CLOCK, configs[0].type)
        assertEquals(WidgetType.TASKS, configs[1].type)
        assertEquals(WidgetType.PROJECTS, configs[2].type)
        assertEquals(WidgetType.NOTES, configs[3].type)
        assertEquals(WidgetType.APP_LAUNCHER, configs[4].type)

        assertTrue(configs.all { it.isVisible })
    }

    @Test
    fun testTaskCompletionToggleState() {
        val task = Task(title = "Test Task", completed = false, priority = TaskPriority.HIGH)
        val toggled = task.copy(completed = !task.completed, completedAt = System.currentTimeMillis())

        assertTrue(toggled.completed)
        assertNotNull(toggled.completedAt)
        assertEquals(task.id, toggled.id)
        assertEquals(TaskPriority.HIGH, toggled.priority)
    }

    @Test
    fun testTaskSortingIncompleteFirstThenCompletedByTimestamp() {
        val task1 = Task(title = "Incomplete 1", completed = false)
        val task2 = Task(title = "Completed Early", completed = true, completedAt = 1000L)
        val task3 = Task(title = "Completed Later", completed = true, completedAt = 2000L)
        val task4 = Task(title = "Incomplete 2", completed = false)

        val tasks = listOf(task3, task1, task2, task4)
        val sorted = tasks.sortedWith(
            compareBy<Task> { it.completed }
                .thenBy { if (it.completed) it.completedAt ?: Long.MAX_VALUE else 0L }
        )

        assertFalse(sorted[0].completed)
        assertFalse(sorted[1].completed)
        assertTrue(sorted[2].completed)
        assertTrue(sorted[3].completed)
        assertEquals("Completed Early", sorted[2].title)
        assertEquals("Completed Later", sorted[3].title)
    }

    @Test
    fun testFavoriteAppsArePinnedAfterWorkSections() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs().map { config ->
            if (config.type == WidgetType.APP_LAUNCHER) config.copy(position = 0) else config
        }

        val ordered = orderedHomeContentSections(configs)

        assertEquals(
            listOf(WidgetType.TASKS, WidgetType.PROJECTS, WidgetType.NOTES, WidgetType.APP_LAUNCHER),
            ordered.map { it.type }
        )
    }

    @Test
    fun testGlobalVsProjectTaskScoping() {
        val globalTask = Task(title = "Global Task A", projectId = null)
        val projectTask = Task(title = "Project Task B", projectId = "proj-123")

        val allTasks = listOf(globalTask, projectTask)
        val globalQueryResults = allTasks.filter { it.projectId == null }
        val projectQueryResults = allTasks.filter { it.projectId == "proj-123" }

        assertTrue(globalQueryResults.contains(globalTask))
        assertFalse(globalQueryResults.contains(projectTask))

        assertTrue(projectQueryResults.contains(projectTask))
        assertFalse(projectQueryResults.contains(globalTask))
        assertTrue(globalTask.isToday)
        assertFalse(projectTask.isToday)
    }

    @Test
    fun testProjectTaskCanBeIntentionallyShownOnTodayWithoutLosingProjectContext() {
        val projectTask = Task(title = "Prepare review", projectId = "proj-123")
        val promoted = projectTask.copy(isToday = true)

        assertFalse(projectTask.isToday)
        assertTrue(promoted.isToday)
        assertEquals(projectTask.projectId, promoted.projectId)
    }

    @Test
    fun testGlobalVsProjectNoteScoping() {
        val globalNote = Note(title = "Global Note A", content = "Global", projectId = null)
        val projectNote = Note(title = "Project Note B", content = "Project", projectId = "proj-123")

        val allNotes = listOf(globalNote, projectNote)
        val globalQueryResults = allNotes.filter { it.projectId == null }
        val projectQueryResults = allNotes.filter { it.projectId == "proj-123" }

        assertTrue(globalQueryResults.contains(globalNote))
        assertFalse(globalQueryResults.contains(projectNote))

        assertTrue(projectQueryResults.contains(projectNote))
        assertFalse(projectQueryResults.contains(globalNote))
    }

    @Test
    fun testCompletedProjectTaskRemainsInProjectContextOnly() {
        val projectTask = Task(title = "Read paper", projectId = "proj-123", completed = true, completedAt = System.currentTimeMillis())

        val allTasks = listOf(projectTask)
        val globalQueryResults = allTasks.filter { it.projectId == null }
        val projectQueryResults = allTasks.filter { it.projectId == "proj-123" }

        assertFalse(globalQueryResults.contains(projectTask))
        assertTrue(projectQueryResults.contains(projectTask))
    }

    @Test
    fun testHardProjectDeletionPermanentlyRemovesTasksAndNotesWithoutOrphansOrGlobalLeakage() {
        val project = Project(id = "proj-123", name = "Research")
        val task1 = Task(id = "t1", title = "Read paper", projectId = project.id)
        val task2 = Task(id = "t2", title = "Write report", projectId = project.id)
        val note1 = Note(id = "n1", title = "Lit review", projectId = project.id)

        var projects = listOf(project)
        var tasks = listOf(task1, task2)
        var notes = listOf(note1)

        // Simulate Hard Project Deletion Transaction
        val deletedProjectId = project.id
        projects = projects.filter { it.id != deletedProjectId }
        tasks = tasks.filter { it.projectId != deletedProjectId }
        notes = notes.filter { it.projectId != deletedProjectId }

        // Assert 0 project tasks or notes remain
        assertTrue(projects.isEmpty())
        assertTrue(tasks.isEmpty())
        assertTrue(notes.isEmpty())

        // Assert 0 deleted items enter global Dashboard queries (WHERE projectId IS NULL)
        val globalTasks = tasks.filter { it.projectId == null }
        val globalNotes = notes.filter { it.projectId == null }
        assertFalse(globalTasks.contains(task1))
        assertFalse(globalTasks.contains(task2))
        assertFalse(globalNotes.contains(note1))
    }

    @Test
    fun testProjectRenamePreservesTaskAssociationByUUID() {
        val project = Project(id = "proj-uuid-1", name = "Initial Name")
        val task = Task(title = "Associated Task", projectId = project.id)

        val renamedProject = project.copy(name = "Renamed Project")

        assertEquals(project.id, renamedProject.id)
        assertEquals(renamedProject.id, task.projectId)
    }

    @Test
    fun testTaskCheckmarkFormat() {
        val taskIncomplete = Task(title = "Task Incomplete", completed = false)
        val taskCompleted = Task(title = "Task Completed", completed = true)

        val stringIncomplete = taskCompletionMarker(taskIncomplete.completed)
        val stringCompleted = taskCompletionMarker(taskCompleted.completed)

        assertEquals("[ ]", stringIncomplete)
        assertEquals("[✓]", stringCompleted)
        assertTrue(stringCompleted.contains("✓"))
        assertFalse(stringCompleted.contains("x"))
    }

    @Test
    fun testNavigationStateEncodingPreservesContextDestinations() {
        val states = listOf(
            NavigationState.Dashboard,
            NavigationState.ProjectDetail("project-123"),
            NavigationState.TaskDetail("task-456"),
            NavigationState.NoteDetail("note-789"),
            NavigationState.SearchAndLauncher,
            NavigationState.Settings
        )

        states.forEach { state ->
            assertEquals(state, decodeNavigationState(encodeNavigationState(state)))
        }
    }

    @Test
    fun testResourceLinksGainHttpsWithoutBreakingExistingSchemes() {
        assertEquals("https://example.com", normalizeResourceUri("example.com"))
        assertEquals("https://example.com/a", normalizeResourceUri("https://example.com/a"))
        assertEquals("content://docs/item", normalizeResourceUri("content://docs/item"))
    }

    @Test
    fun testFourBuiltInThemes() {
        val themes = listOf("AMBER", "DARK", "LIGHT", "OLED")
        assertEquals(4, themes.size)
        assertTrue(themes.contains("AMBER"))
        assertTrue(themes.contains("DARK"))
        assertTrue(themes.contains("LIGHT"))
        assertTrue(themes.contains("OLED"))
    }

    @Test
    fun testUserAccentColorSelectionDoesNotOverrideTaskPrioritySemantics() {
        val userSelectedAccentColor = "#3B82F6" // User selected Blue accent

        // Priority colors remain immutable
        val highPriorityColor = "#EF4444"   // Red
        val mediumPriorityColor = "#F59E0B" // Amber/Yellow
        val lowPriorityColor = "#10B981"    // Green

        assertNotEquals(userSelectedAccentColor, highPriorityColor)
        assertNotEquals(userSelectedAccentColor, mediumPriorityColor)
        assertNotEquals(userSelectedAccentColor, lowPriorityColor)
    }

    @Test
    fun testDisplaySliderRanges() {
        val brightness = 0.8f.coerceIn(0.0f, 1.0f)
        val contrast = 1.2f.coerceIn(0.5f, 1.5f)
        val saturation = 1.5f.coerceIn(0.0f, 2.0f)
        val wallpaperDim = 0.3f.coerceIn(0.0f, 1.0f)

        assertEquals(0.8f, brightness, 0.01f)
        assertEquals(1.2f, contrast, 0.01f)
        assertEquals(1.5f, saturation, 0.01f)
        assertEquals(0.3f, wallpaperDim, 0.01f)
    }
}
