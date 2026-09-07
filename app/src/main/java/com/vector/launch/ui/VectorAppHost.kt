package com.vector.launch.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vector.launch.core.database.VectorDatabase
import com.vector.launch.core.datastore.PreferencesRepository
import com.vector.launch.core.model.SearchResultType
import com.vector.launch.core.repository.AppLauncherRepository
import com.vector.launch.core.repository.BackupRepository
import com.vector.launch.core.repository.NoteRepository
import com.vector.launch.core.repository.ProjectRepository
import com.vector.launch.core.repository.ResourceRepository
import com.vector.launch.core.repository.SearchRepository
import com.vector.launch.core.repository.TaskRepository
import com.vector.launch.core.theme.VectorTheme
import com.vector.launch.dashboard.DashboardViewModel
import com.vector.launch.dashboard.VectorNHomeScreen
import com.vector.launch.notes.NoteEditorScreen
import com.vector.launch.notes.NoteEditorViewModel
import com.vector.launch.projects.ProjectDetailScreen
import com.vector.launch.projects.ProjectDetailViewModel
import com.vector.launch.search.SearchCaptureScreen
import com.vector.launch.search.SearchCaptureViewModel
import com.vector.launch.settings.SettingsScreen
import com.vector.launch.settings.SettingsViewModel
import com.vector.launch.tasks.TaskDetailScreen
import com.vector.launch.tasks.TaskDetailViewModel

sealed class NavigationState {
    object Dashboard : NavigationState()
    data class TaskDetail(val taskId: String) : NavigationState()
    data class ProjectDetail(val projectId: String) : NavigationState()
    data class NoteDetail(val noteId: String) : NavigationState()
    object SearchAndLauncher : NavigationState()
    object Settings : NavigationState()
}

internal fun encodeNavigationState(state: NavigationState): String = when (state) {
    NavigationState.Dashboard -> "dashboard"
    is NavigationState.TaskDetail -> "task|${state.taskId}"
    is NavigationState.ProjectDetail -> "project|${state.projectId}"
    is NavigationState.NoteDetail -> "note|${state.noteId}"
    NavigationState.SearchAndLauncher -> "search"
    NavigationState.Settings -> "settings"
}

internal fun decodeNavigationState(encoded: String): NavigationState? {
    val parts = encoded.split('|', limit = 2)
    return when (parts.firstOrNull()) {
        "dashboard" -> NavigationState.Dashboard
        "task" -> parts.getOrNull(1)?.takeIf(String::isNotBlank)?.let(NavigationState::TaskDetail)
        "project" -> parts.getOrNull(1)?.takeIf(String::isNotBlank)?.let(NavigationState::ProjectDetail)
        "note" -> parts.getOrNull(1)?.takeIf(String::isNotBlank)?.let(NavigationState::NoteDetail)
        "search" -> NavigationState.SearchAndLauncher
        "settings" -> NavigationState.Settings
        else -> null
    }
}

private val NavigationPathSaver = Saver<List<NavigationState>, List<String>>(
    save = { states -> states.map(::encodeNavigationState) },
    restore = { saved -> saved.mapNotNull(::decodeNavigationState).ifEmpty { listOf(NavigationState.Dashboard) } }
)

private class VectorViewModelFactory<VM : ViewModel>(
    private val createViewModel: () -> VM
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = createViewModel() as T
}

@Composable
fun VectorAppHost() {
    val context = LocalContext.current
    val database = remember { VectorDatabase.getInstance(context) }
    val preferencesRepository = remember { PreferencesRepository(context) }

    val appLauncherRepository = remember { AppLauncherRepository(context) }
    val backupRepository = remember { BackupRepository(database, preferencesRepository) }
    val taskRepository = remember { TaskRepository(database.taskDao()) }
    val projectRepository = remember { ProjectRepository(database.projectDao()) }
    val noteRepository = remember { NoteRepository(database.noteDao()) }
    val resourceRepository = remember { ResourceRepository(database.resourceDao()) }
    val searchRepository = remember {
        SearchRepository(
            database.taskDao(),
            database.projectDao(),
            database.noteDao(),
            database.resourceDao(),
            appLauncherRepository
        )
    }

    val themeMode by preferencesRepository.themeMode.collectAsStateWithLifecycle(initialValue = "OLED")
    val accentColorHex by preferencesRepository.accentColorHex.collectAsStateWithLifecycle(initialValue = "#A5B4FC")

    var navigationPath by rememberSaveable(stateSaver = NavigationPathSaver) {
        mutableStateOf(listOf<NavigationState>(NavigationState.Dashboard))
    }
    val navigationState = navigationPath.last()
    val navigateTo: (NavigationState) -> Unit = { destination ->
        navigationPath = navigationPath + destination
    }
    val navigateBack: () -> Unit = {
        navigationPath = if (navigationPath.size > 1) navigationPath.dropLast(1) else navigationPath
    }

    // ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = remember {
            VectorViewModelFactory {
                DashboardViewModel(taskRepository, projectRepository, noteRepository, preferencesRepository, appLauncherRepository)
            }
        }
    )
    val searchCaptureViewModel: SearchCaptureViewModel = viewModel(
        factory = remember {
            VectorViewModelFactory { SearchCaptureViewModel(searchRepository, taskRepository, noteRepository) }
        }
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = remember {
            VectorViewModelFactory { SettingsViewModel(preferencesRepository, backupRepository) }
        }
    )
    val projectDetailViewModel: ProjectDetailViewModel = viewModel(
        factory = remember {
            VectorViewModelFactory {
                ProjectDetailViewModel(projectRepository, taskRepository, noteRepository, resourceRepository)
            }
        }
    )
    val noteEditorViewModel: NoteEditorViewModel = viewModel(
        factory = remember {
            VectorViewModelFactory { NoteEditorViewModel(noteRepository) }
        }
    )
    val taskDetailViewModel: TaskDetailViewModel = viewModel(
        factory = remember {
            VectorViewModelFactory {
                TaskDetailViewModel(taskRepository, projectRepository, noteRepository, resourceRepository)
            }
        }
    )

    VectorTheme(themeMode = themeMode, accentColorHex = accentColorHex) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = navigationState, label = "ScreenTransition") { state ->
                when (state) {
                    is NavigationState.Dashboard -> VectorNHomeScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToTask = { taskId ->
                            navigateTo(NavigationState.TaskDetail(taskId))
                        },
                        onNavigateToProject = { projectId ->
                            navigateTo(NavigationState.ProjectDetail(projectId))
                        },
                        onNavigateToNote = { noteId ->
                            navigateTo(NavigationState.NoteDetail(noteId))
                        },
                        onNavigateToSearch = {
                            navigateTo(NavigationState.SearchAndLauncher)
                        },
                        onNavigateToSettings = {
                            navigateTo(NavigationState.Settings)
                        }
                    )

                    is NavigationState.TaskDetail -> {
                        TaskDetailScreen(
                            viewModel = taskDetailViewModel,
                            taskId = state.taskId,
                            onBackClick = navigateBack
                        )
                    }

                    is NavigationState.ProjectDetail -> ProjectDetailScreen(
                        viewModel = projectDetailViewModel,
                        projectId = state.projectId,
                        onBack = navigateBack,
                        onNavigateToTask = { taskId ->
                            navigateTo(NavigationState.TaskDetail(taskId))
                        },
                        onNavigateToNote = { noteId ->
                            navigateTo(NavigationState.NoteDetail(noteId))
                        }
                    )

                    is NavigationState.NoteDetail -> NoteEditorScreen(
                        viewModel = noteEditorViewModel,
                        noteId = state.noteId,
                        onBack = navigateBack
                    )

                    is NavigationState.SearchAndLauncher -> SearchCaptureScreen(
                        viewModel = searchCaptureViewModel,
                        onBackClick = navigateBack,
                        onResultSelected = { result ->
                            when (result.type) {
                                SearchResultType.APPLICATION -> {
                                    result.packageName?.let { pkg ->
                                        appLauncherRepository.launchApp(pkg)
                                    }
                                }
                                SearchResultType.TASK -> {
                                    navigateTo(NavigationState.TaskDetail(result.id))
                                }
                                SearchResultType.PROJECT -> {
                                    navigateTo(NavigationState.ProjectDetail(result.id))
                                }
                                SearchResultType.NOTE -> {
                                    navigateTo(NavigationState.NoteDetail(result.id))
                                }
                                SearchResultType.RESOURCE -> {
                                    if (result.projectId != null) {
                                        navigateTo(NavigationState.ProjectDetail(result.projectId))
                                    }
                                }
                            }
                        }
                    )

                    is NavigationState.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        onBackClick = navigateBack
                    )
                }
            }
        }
    }
}
