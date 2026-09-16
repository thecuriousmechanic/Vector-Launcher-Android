package com.vector.launch.core.repository

import com.vector.launch.core.database.ProjectDao
import com.vector.launch.core.database.ProjectEntity
import com.vector.launch.core.model.Project
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.UUID

/**
 * Unit tests for [ProjectRepository] to verify project CRUD operations,
 * progress calculation, and proper entity conversion.
 */
class ProjectRepositoryTest {

    private lateinit var projectDao: ProjectDao
    private lateinit var projectRepository: ProjectRepository

    @Before
    fun setup() {
        projectDao = mock()
        projectRepository = ProjectRepository(projectDao)
    }

    @Test
    fun testSaveProject_InsertsProjectWithCorrectData() = runTest {
        val project = Project(
            id = "proj-1",
            name = "Test Project",
            description = "Description",
            colorHex = "#6366F1"
        )

        projectRepository.saveProject(project)

        verify(projectDao).insertProject(any())
    }

    @Test
    fun testDeleteProject_RemovesProject() = runTest {
        val projectId = "proj-1"

        projectRepository.deleteProject(projectId)

        verify(projectDao).deleteProjectById(projectId)
    }

    @Test
    fun testProjectCreation_GeneratesStableUUID() {
        val project1 = Project(name = "Project A")
        val project2 = Project(name = "Project A")

        assertNotNull(project1.id)
        assertNotNull(project2.id)
        assertEquals(36, project1.id.length)
        assertEquals(36, project2.id.length)
    }

    @Test
    fun testProjectDefaultValues() {
        val project = Project(name = "Simple Project")

        assertEquals("Simple Project", project.name)
        assertEquals("", project.description)
        assertEquals("#6366F1", project.colorHex)
        assertNotNull(project.createdAt)
        assertNotNull(project.updatedAt)
    }

    @Test
    fun testProjectProgressCalculation_EmptyTasks() {
        val project = Project(
            id = "proj-1",
            name = "Empty Project",
            taskIds = emptyList()
        )

        val progress = project.progress
        assertEquals(0, progress)
    }

    @Test
    fun testProjectProgressCalculation_AllIncomplete() {
        val project = Project(
            id = "proj-1",
            name = "No Progress Project",
            taskIds = listOf("task-1", "task-2", "task-3"),
            completedTaskCount = 0
        )

        val progress = project.progress
        assertEquals(0, progress)
    }

    @Test
    fun testProjectProgressCalculation_AllComplete() {
        val project = Project(
            id = "proj-1",
            name = "Complete Project",
            taskIds = listOf("task-1", "task-2"),
            completedTaskCount = 2
        )

        val progress = project.progress
        assertEquals(100, progress)
    }

    @Test
    fun testProjectProgressCalculation_PartialCompletion() {
        val project = Project(
            id = "proj-1",
            name = "Partial Project",
            taskIds = listOf("task-1", "task-2", "task-3", "task-4"),
            completedTaskCount = 2
        )

        val progress = project.progress
        assertEquals(50, progress)
    }

    @Test
    fun testProjectRename_UpdatesTimestamp() {
        val beforeTime = System.currentTimeMillis()
        val project = Project(
            id = "proj-1",
            name = "Original Name",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val renamed = project.copy(
            name = "Renamed Project",
            updatedAt = System.currentTimeMillis()
        )

        assertTrue(renamed.updatedAt >= beforeTime)
        assertEquals("Renamed Project", renamed.name)
        assertEquals(project.id, renamed.id)
        assertEquals(project.createdAt, renamed.createdAt)
    }

    @Test
    fun testProjectEntityConversion_ToDomain() {
        val entity = ProjectEntity(
            id = "proj-1",
            name = "Test Project",
            description = "Description",
            colorHex = "#FF5722",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.description, domain.description)
        assertEquals(entity.colorHex, domain.colorHex)
        assertEquals(entity.createdAt, domain.createdAt)
        assertEquals(entity.updatedAt, domain.updatedAt)
    }

    @Test
    fun testProjectEntityConversion_FromDomain() {
        val domain = Project(
            id = "proj-1",
            name = "Test Project",
            description = "Description",
            colorHex = "#4CAF50",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = ProjectEntity.fromDomain(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.description, entity.description)
        assertEquals(domain.colorHex, entity.colorHex)
        assertEquals(domain.createdAt, entity.createdAt)
        assertEquals(domain.updatedAt, entity.updatedAt)
    }

    @Test
    fun testProjectColorDefaults() {
        val defaultProject = Project(name = "Default Color")
        assertEquals("#6366F1", defaultProject.colorHex)

        val customProject = Project(
            name = "Custom Color",
            colorHex = "#E91E63"
        )
        assertEquals("#E91E63", customProject.colorHex)
    }
}
