package com.vector.launch

import com.vector.launch.core.model.AppInfo
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.SearchResult
import com.vector.launch.core.model.SearchResultType
import com.vector.launch.core.model.Task
import com.vector.launch.dashboard.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class VectorPerformanceTest {

    @Test
    fun measureDashboardUiStateInstantiation() {
        val tasks = List(100) { Task(title = "Task $it") }
        val projects = List(20) { Project(name = "Project $it") }
        val notes = List(50) { Note(title = "Note $it", content = "Content $it") }

        val elapsedNs = measureNanoTime {
            val state = DashboardUiState(
                activeTasks = tasks,
                projects = projects,
                recentNotes = notes
            )
            assertEquals(100, state.activeTasks.size)
        }

        val elapsedMs = elapsedNs / 1_000_000.0
        println("BENCHMARK [Dashboard UI State Creation]: $elapsedMs ms")
        assertTrue("Dashboard State creation must be under 5 ms", elapsedMs < 5.0)
    }

    @Test
    fun measureSearchFilteringLatency() {
        val results = mutableListOf<SearchResult>()
        repeat(500) {
            results.add(SearchResult(id = "task-$it", type = SearchResultType.TASK, title = "Task $it"))
            results.add(SearchResult(id = "app-$it", type = SearchResultType.APPLICATION, title = "App $it", packageName = "com.app.$it"))
            results.add(SearchResult(id = "proj-$it", type = SearchResultType.PROJECT, title = "Project $it"))
        }

        val elapsedMs = measureTimeMillis {
            val filtered = results.filter { it.type == SearchResultType.APPLICATION }
            assertEquals(500, filtered.size)
        }

        println("BENCHMARK [Search 1500 Items Filtering]: $elapsedMs ms")
        assertTrue("In-memory search filtering must be under 15 ms", elapsedMs < 15)
    }

    @Test
    fun measureClockFormattingEfficiency() {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Date()

        val elapsedMs = measureTimeMillis {
            repeat(10_000) {
                val formatted = formatter.format(now)
                assertEquals(5, formatted.length)
            }
        }

        println("BENCHMARK [Clock 10k Formatting Iterations]: $elapsedMs ms")
        assertTrue("Clock formatting must take < 50 ms for 10k iterations", elapsedMs < 50)
    }

    @Test
    fun measureAppLauncherInfoSortingPerformance() {
        val apps = List(200) {
            AppInfo(packageName = "com.example.app$it", label = "Application Label $it")
        }.shuffled()

        val elapsedMs = measureTimeMillis {
            val sorted = apps.sortedBy { it.label.lowercase() }
            assertEquals(200, sorted.size)
        }

        println("BENCHMARK [App Launcher 200 Apps Sorting]: $elapsedMs ms")
        assertTrue("App sorting must take < 20 ms", elapsedMs < 20)
    }
}
