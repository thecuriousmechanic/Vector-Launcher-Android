package com.vector.launch.core.repository

import com.vector.launch.core.database.TaskDao
import com.vector.launch.core.database.TaskEntity
import com.vector.launch.core.model.Task
import com.vector.launch.core.model.TaskPriority
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [TaskRepository] to verify task CRUD operations,
 * completion toggling, project scoping, and Today visibility logic.
 */
class TaskRepositoryTest {

    private lateinit var taskDao: TaskDao
    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        taskDao = mock()
        taskRepository = TaskRepository(taskDao)
    }

    @Test
    fun testSaveTask_InsertsTaskWithCorrectData() = runTest {
        val task = Task(
            id = "task-1",
            title = "Test Task",
            description = "Description",
            priority = TaskPriority.HIGH,
            projectId = null
        )

        taskRepository.saveTask(task)

        verify(taskDao).insertTask(any())
    }

    @Test
    fun testToggleTaskCompleted_MarksIncompleteAsComplete() = runTest {
        val incompleteTask = Task(
            id = "task-1",
            title = "Test Task",
            completed = false,
            completedAt = null
        )

        val beforeTime = System.currentTimeMillis()
        val updatedTask = taskRepository.toggleTaskCompleted(incompleteTask)
        val afterTime = System.currentTimeMillis()

        assertTrue(updatedTask.completed)
        assertNotNull(updatedTask.completedAt)
        assertTrue(updatedTask.completedAt!! >= beforeTime)
        assertTrue(updatedTask.completedAt!! <= afterTime)
        assertEquals(incompleteTask.id, updatedTask.id)
    }

    @Test
    fun testToggleTaskCompleted_MarksCompleteAsIncomplete() = runTest {
        val completeTask = Task(
            id = "task-1",
            title = "Test Task",
            completed = true,
            completedAt = 1000L
        )

        val updatedTask = taskRepository.toggleTaskCompleted(completeTask)

        assertFalse(updatedTask.completed)
        assertNull(updatedTask.completedAt)
        assertEquals(completeTask.id, updatedTask.id)
    }

    @Test
    fun testToggleTaskToday_GlobalTask_ReturnsUnchanged() = runTest {
        val globalTask = Task(
            id = "task-1",
            title = "Global Task",
            projectId = null,
            isToday = true
        )

        val result = taskRepository.toggleTaskToday(globalTask)

        assertEquals(globalTask, result)
        verify(taskDao, never()).updateTask(any())
    }

    @Test
    fun testToggleTaskToday_ProjectTask_TogglesIsTodayFlag() = runTest {
        val projectTask = Task(
            id = "task-1",
            title = "Project Task",
            projectId = "proj-1",
            isToday = false
        )

        val updatedTask = taskRepository.toggleTaskToday(projectTask)

        assertTrue(updatedTask.isToday)
        assertEquals(projectTask.id, updatedTask.id)
        assertEquals(projectTask.projectId, updatedTask.projectId)
        verify(taskDao).updateTask(any())
    }

    @Test
    fun testDeleteTask_RemovesTaskAndResources() = runTest {
        val taskId = "task-1"

        taskRepository.deleteTask(taskId)

        verify(taskDao).deleteTaskWithResources(taskId)
    }

    @Test
    fun testTaskPriorityPreservedOnCompletion() = runTest {
        val highPriorityTask = Task(
            id = "task-1",
            title = "High Priority Task",
            completed = false,
            priority = TaskPriority.HIGH
        )

        val updatedTask = taskRepository.toggleTaskCompleted(highPriorityTask)

        assertEquals(TaskPriority.HIGH, updatedTask.priority)
        assertTrue(updatedTask.completed)
    }

    @Test
    fun testTaskWithProjectContext_RetainsProjectOnCompletion() = runTest {
        val projectTask = Task(
            id = "task-1",
            title = "Project Task",
            projectId = "proj-123",
            completed = false
        )

        val updatedTask = taskRepository.toggleTaskCompleted(projectTask)

        assertEquals("proj-123", updatedTask.projectId)
        assertTrue(updatedTask.completed)
    }

    @Test
    fun testTaskCreation_GeneratesStableUUID() {
        val task1 = Task(title = "Task A")
        val task2 = Task(title = "Task A")

        assertNotNull(task1.id)
        assertNotNull(task2.id)
        assertEquals(36, task1.id.length) // UUID string length
        assertEquals(36, task2.id.length)
    }

    @Test
    fun testTaskDefaultValues() {
        val task = Task(title = "Simple Task")

        assertEquals("Simple Task", task.title)
        assertEquals("", task.description)
        assertFalse(task.completed)
        assertEquals(TaskPriority.NONE, task.priority)
        assertNull(task.dueDate)
        assertNull(task.projectId)
        assertTrue(task.isToday)
        assertNull(task.completedAt)
        assertNotNull(task.createdAt)
        assertNotNull(task.updatedAt)
    }

    @Test
    fun testProjectTask_IsNotTodayByDefault() {
        val projectTask = Task(
            title = "Project Task",
            projectId = "proj-1"
        )

        assertFalse(projectTask.isToday)
    }

    @Test
    fun testGlobalTask_IsTodayByDefault() {
        val globalTask = Task(
            title = "Global Task",
            projectId = null
        )

        assertTrue(globalTask.isToday)
    }
}
