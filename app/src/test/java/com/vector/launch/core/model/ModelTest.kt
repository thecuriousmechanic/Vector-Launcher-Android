package com.vector.launch.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for domain models to verify data integrity,
 * default values, and business logic.
 */
class ModelTest {

    @Test
    fun testTaskCreationWithAllFields() {
        val task = Task(
            id = "task-1",
            title = "Test Task",
            description = "Description",
            completed = false,
            priority = TaskPriority.HIGH,
            dueDate = 1234567890L,
            projectId = "proj-1",
            isToday = true,
            completedAt = null,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        assertEquals("task-1", task.id)
        assertEquals("Test Task", task.title)
        assertEquals("Description", task.description)
        assertFalse(task.completed)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals(1234567890L, task.dueDate)
        assertEquals("proj-1", task.projectId)
        assertTrue(task.isToday)
        assertNull(task.completedAt)
        assertEquals(1000L, task.createdAt)
        assertEquals(2000L, task.updatedAt)
    }

    @Test
    fun testTaskCompletionMarkerFormat() {
        val incompleteTask = Task(title = "Incomplete", completed = false)
        val completeTask = Task(title = "Complete", completed = true)

        val incompleteMarker = taskCompletionMarker(incompleteTask.completed)
        val completeMarker = taskCompletionMarker(completeTask.completed)

        assertEquals("[ ]", incompleteMarker)
        assertEquals("[✓]", completeMarker)
    }

    @Test
    fun testTaskPriorityColorMapping() {
        assertEquals("#EF4444", TaskPriority.HIGH.colorHex)
        assertEquals("#F59E0B", TaskPriority.MEDIUM.colorHex)
        assertEquals("#10B981", TaskPriority.LOW.colorHex)
        assertEquals("#9CA3AF", TaskPriority.NONE.colorHex)
    }

    @Test
    fun testProjectProgressCalculation() {
        val projectZero = Project(
            id = "p1", name = "P1", taskIds = listOf("t1", "t2"), completedTaskCount = 0
        )
        val projectHalf = Project(
            id = "p2", name = "P2", taskIds = listOf("t1", "t2"), completedTaskCount = 1
        )
        val projectFull = Project(
            id = "p3", name = "P3", taskIds = listOf("t1", "t2"), completedTaskCount = 2
        )

        assertEquals(0, projectZero.progress)
        assertEquals(50, projectHalf.progress)
        assertEquals(100, projectFull.progress)
    }

    @Test
    fun testProjectWithEmptyTasks() {
        val project = Project(
            id = "p1",
            name = "Empty Project",
            taskIds = emptyList(),
            completedTaskCount = 0
        )

        assertEquals(0, project.progress)
        assertTrue(project.taskIds.isEmpty())
    }

    @Test
    fun testNoteCreationWithAllFields() {
        val note = Note(
            id = "note-1",
            title = "Test Note",
            content = "Content here",
            projectId = "proj-1",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        assertEquals("note-1", note.id)
        assertEquals("Test Note", note.title)
        assertEquals("Content here", note.content)
        assertEquals("proj-1", note.projectId)
        assertEquals(1000L, note.createdAt)
        assertEquals(2000L, note.updatedAt)
    }

    @Test
    fun testNoteDefaultValues() {
        val note = Note(title = "Simple Note", content = "Content")

        assertNotNull(note.id)
        assertEquals("Simple Note", note.title)
        assertEquals("Content", note.content)
        assertNull(note.projectId)
        assertNotNull(note.createdAt)
        assertNotNull(note.updatedAt)
        assertTrue(note.updatedAt >= note.createdAt)
    }

    @Test
    fun testResourceEntityTypes() {
        val link = Resource(
            id = "r1",
            title = "Website",
            uri = "https://example.com",
            type = "LINK"
        )

        val file = Resource(
            id = "r2",
            title = "Document",
            uri = "content://docs/file.pdf",
            type = "FILE"
        )

        assertEquals("LINK", link.type)
        assertEquals("FILE", file.type)
    }

    @Test
    fun testResourceNormalization() {
        assertEquals("https://example.com", normalizeResourceUri("example.com"))
        assertEquals("https://example.com/path", normalizeResourceUri("https://example.com/path"))
        assertEquals("content://local/file", normalizeResourceUri("content://local/file"))
        assertEquals("file:///storage/doc", normalizeResourceUri("file:///storage/doc"))
    }

    @Test
    fun testAppInfoDefaults() {
        val app = AppInfo(
            packageName = "com.example.app",
            label = "Example App"
        )

        assertEquals("com.example.app", app.packageName)
        assertEquals("Example App", app.label)
        assertNull(app.iconBitmap)
    }

    @Test
    fun testSearchResultTypes() {
        val taskResult = SearchResult(
            id = "t1",
            type = SearchResultType.TASK,
            title = "Task Result"
        )
        val appResult = SearchResult(
            id = "a1",
            type = SearchResultType.APPLICATION,
            title = "App Result",
            packageName = "com.app"
        )
        val projectResult = SearchResult(
            id = "p1",
            type = SearchResultType.PROJECT,
            title = "Project Result"
        )
        val noteResult = SearchResult(
            id = "n1",
            type = SearchResultType.NOTE,
            title = "Note Result"
        )
        val resourceResult = SearchResult(
            id = "r1",
            type = SearchResultType.RESOURCE,
            title = "Resource Result"
        )

        assertEquals(SearchResultType.TASK, taskResult.type)
        assertEquals(SearchResultType.APPLICATION, appResult.type)
        assertEquals(SearchResultType.PROJECT, projectResult.type)
        assertEquals(SearchResultType.NOTE, noteResult.type)
        assertEquals(SearchResultType.RESOURCE, resourceResult.type)
    }

    @Test
    fun testWidgetTypeValues() {
        val types = WidgetType.entries.map { it.name }

        assertTrue(types.contains("CLOCK"))
        assertTrue(types.contains("TASKS"))
        assertTrue(types.contains("PROJECTS"))
        assertTrue(types.contains("NOTES"))
        assertTrue(types.contains("APP_LAUNCHER"))
        assertEquals(5, WidgetType.entries.size)
    }

    @Test
    fun testDashboardWidgetConfigDefaults() {
        val config = DashboardWidgetConfig(
            id = "w1",
            type = WidgetType.TASKS,
            title = "Tasks"
        )

        assertEquals("w1", config.id)
        assertEquals(WidgetType.TASKS, config.type)
        assertEquals("Tasks", config.title)
        assertEquals(0, config.position)
        assertTrue(config.isVisible)
        assertTrue(config.isExpanded)
    }

    @Test
    fun testTaskCopyPreservesIdentity() {
        val original = Task(
            id = "task-1",
            title = "Original",
            createdAt = 1000L
        )

        val modified = original.copy(
            title = "Modified",
            completed = true,
            completedAt = 2000L
        )

        assertEquals(original.id, modified.id)
        assertEquals(original.createdAt, modified.createdAt)
        assertEquals("Modified", modified.title)
        assertTrue(modified.completed)
    }

    @Test
    fun testProjectCopyPreservesIdentity() {
        val original = Project(
            id = "proj-1",
            name = "Original",
            createdAt = 1000L
        )

        val modified = original.copy(
            name = "Renamed",
            updatedAt = 2000L
        )

        assertEquals(original.id, modified.id)
        assertEquals(original.createdAt, modified.createdAt)
        assertEquals("Renamed", modified.name)
    }

    @Test
    fun testNoteCopyPreservesIdentity() {
        val original = Note(
            id = "note-1",
            title = "Original",
            createdAt = 1000L
        )

        val modified = original.copy(
            title = "Updated",
            content = "New content"
        )

        assertEquals(original.id, modified.id)
        assertEquals(original.createdAt, modified.createdAt)
        assertEquals("Updated", modified.title)
        assertEquals("New content", modified.content)
    }
}
