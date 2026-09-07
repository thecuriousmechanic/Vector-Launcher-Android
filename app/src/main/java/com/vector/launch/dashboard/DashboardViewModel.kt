package com.vector.launch.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.launch.core.datastore.PreferencesRepository
import com.vector.launch.core.model.AppInfo
import com.vector.launch.core.model.DashboardWidgetConfig
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.Task
import com.vector.launch.core.repository.AppLauncherRepository
import com.vector.launch.core.repository.NoteRepository
import com.vector.launch.core.repository.ProjectRepository
import com.vector.launch.core.repository.TaskRepository
import com.vector.launch.core.model.TaskPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val widgetConfigs: List<DashboardWidgetConfig> = emptyList(),
    val activeTasks: List<Task> = emptyList(),
    val projects: List<Project> = emptyList(),
    val recentNotes: List<Note> = emptyList(),
    val preferredAppPackages: List<String> = emptyList(),
    val preferredApps: List<AppInfo> = emptyList(),
    val allInstalledApps: List<AppInfo> = emptyList(),
    val themeMode: String = "OLED",
    val accentColorHex: String = "#A5B4FC",
    val brightnessValue: Float = 1.0f,
    val contrastValue: Float = 1.0f,
    val saturationValue: Float = 1.0f,
    val wallpaperDimValue: Float = 0.20f,
    val wallpaperUri: String? = null,
    val selectedContextApp: AppInfo? = null,
    val isAppPickerOpen: Boolean = false,
    val isAppBrowserOpen: Boolean = false,
    val appBrowserTargetLetter: Char? = null,
    val isCreateProjectOpen: Boolean = false,
    val isCreateTaskOpen: Boolean = false,
    val isLoading: Boolean = false
)

internal fun resolvePreferredApps(
    preferredPackages: List<String>,
    installedApps: List<AppInfo>
): List<AppInfo> {
    if (preferredPackages.isEmpty() || installedApps.isEmpty()) return emptyList()
    val appsByPackage = installedApps.associateBy(AppInfo::packageName)
    return preferredPackages.distinct().mapNotNull(appsByPackage::get)
}

class DashboardViewModel(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val noteRepository: NoteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val appLauncherRepository: AppLauncherRepository
) : ViewModel() {

    private val isAppPickerOpenState = MutableStateFlow(false)
    private val isAppBrowserOpenState = MutableStateFlow(false)
    private val appBrowserTargetLetterState = MutableStateFlow<Char?>(null)
    private val isCreateProjectOpenState = MutableStateFlow(false)
    private val isCreateTaskOpenState = MutableStateFlow(false)
    private val selectedContextAppState = MutableStateFlow<AppInfo?>(null)
    private val allInstalledAppsState = MutableStateFlow<List<AppInfo>>(emptyList())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<DashboardUiState> = combine<Any?, DashboardUiState>(
        preferencesRepository.widgetConfigs,
        taskRepository.getGlobalTasks(),
        projectRepository.getAllProjects(),
        noteRepository.getGlobalNotes(),
        preferencesRepository.preferredAppPackages,
        preferencesRepository.wallpaperUri,
        preferencesRepository.themeMode,
        preferencesRepository.accentColorHex,
        preferencesRepository.brightnessValue,
        preferencesRepository.contrastValue,
        preferencesRepository.saturationValue,
        preferencesRepository.wallpaperDimValue,
        isAppPickerOpenState,
        isAppBrowserOpenState,
        appBrowserTargetLetterState,
        isCreateProjectOpenState,
        isCreateTaskOpenState,
        selectedContextAppState,
        allInstalledAppsState
    ) { flows: Array<Any?> ->
        val configs = flows[0] as List<DashboardWidgetConfig>
        val tasks = flows[1] as List<Task>
        val projects = flows[2] as List<Project>
        val notes = flows[3] as List<Note>
        val prefPackages = flows[4] as List<String>
        val wallpaperUri = flows[5] as String?
        val themeMode = flows[6] as String
        val accentHex = flows[7] as String
        val brightnessVal = flows[8] as Float
        val contrastVal = flows[9] as Float
        val saturationVal = flows[10] as Float
        val dimVal = flows[11] as Float
        val isAppPickerOpen = flows[12] as Boolean
        val isAppBrowserOpen = flows[13] as Boolean
        val appBrowserTargetLetter = flows[14] as Char?
        val isCreateProjectOpen = flows[15] as Boolean
        val isCreateTaskOpen = flows[16] as Boolean
        val selectedContextApp = flows[17] as AppInfo?
        val allApps = flows[18] as List<AppInfo>

        DashboardUiState(
            widgetConfigs = configs,
            activeTasks = tasks,
            projects = projects,
            recentNotes = notes,
            preferredAppPackages = prefPackages,
            preferredApps = resolvePreferredApps(prefPackages, allApps),
            allInstalledApps = allApps,
            themeMode = themeMode,
            accentColorHex = accentHex,
            brightnessValue = brightnessVal,
            contrastValue = contrastVal,
            saturationValue = saturationVal,
            wallpaperDimValue = dimVal,
            wallpaperUri = wallpaperUri,
            selectedContextApp = selectedContextApp,
            isAppPickerOpen = isAppPickerOpen,
            isAppBrowserOpen = isAppBrowserOpen,
            appBrowserTargetLetter = appBrowserTargetLetter,
            isCreateProjectOpen = isCreateProjectOpen,
            isCreateTaskOpen = isCreateTaskOpen,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(
            widgetConfigs = PreferencesRepository.getDefaultWidgetConfigs(),
            isLoading = true
        )
    )

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = appLauncherRepository.getInstalledApps()
            allInstalledAppsState.value = apps
        }
    }

    fun openAppPicker() {
        viewModelScope.launch {
            allInstalledAppsState.value = appLauncherRepository.getInstalledApps(forceRefresh = true)
            isAppPickerOpenState.value = true
        }
    }

    fun closeAppPicker() {
        isAppPickerOpenState.value = false
    }

    fun openAppBrowser() {
        appBrowserTargetLetterState.value = null
        isAppBrowserOpenState.value = true
        refreshInstalledApps()
    }

    fun scrubAppBrowserTo(letter: Char) {
        appBrowserTargetLetterState.value = letter
        isAppBrowserOpenState.value = true
        if (allInstalledAppsState.value.isEmpty()) loadInstalledApps()
    }

    fun finishAppBrowserScrub() {
        appBrowserTargetLetterState.value = null
    }

    fun closeAppBrowser() {
        isAppBrowserOpenState.value = false
        appBrowserTargetLetterState.value = null
    }

    private fun refreshInstalledApps() {
        viewModelScope.launch {
            allInstalledAppsState.value = appLauncherRepository.getInstalledApps(forceRefresh = true)
        }
    }

    fun openAppContextMenu(app: AppInfo) {
        selectedContextAppState.value = app
    }

    fun closeAppContextMenu() {
        selectedContextAppState.value = null
    }

    fun toggleFavoriteApp(packageName: String) {
        viewModelScope.launch {
            val current = uiState.value.preferredAppPackages.toMutableList()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            preferencesRepository.savePreferredAppPackages(current)
        }
    }

    fun savePreferredAppPackages(packages: List<String>) {
        viewModelScope.launch {
            preferencesRepository.savePreferredAppPackages(packages)
            isAppPickerOpenState.value = false
        }
    }

    fun movePreferredAppLeft(packageName: String) {
        viewModelScope.launch {
            val current = uiState.value.preferredAppPackages.toMutableList()
            val index = current.indexOf(packageName)
            if (index > 0) {
                val item = current.removeAt(index)
                current.add(index - 1, item)
                preferencesRepository.savePreferredAppPackages(current)
            }
        }
    }

    fun movePreferredAppRight(packageName: String) {
        viewModelScope.launch {
            val current = uiState.value.preferredAppPackages.toMutableList()
            val index = current.indexOf(packageName)
            if (index >= 0 && index < current.size - 1) {
                val item = current.removeAt(index)
                current.add(index + 1, item)
                preferencesRepository.savePreferredAppPackages(current)
            }
        }
    }

    fun removePreferredApp(packageName: String) {
        viewModelScope.launch {
            val current = uiState.value.preferredAppPackages.toMutableList()
            if (current.remove(packageName)) {
                preferencesRepository.savePreferredAppPackages(current)
            }
        }
    }

    fun openCreateProjectDialog() {
        isCreateProjectOpenState.value = true
    }

    fun closeCreateProjectDialog() {
        isCreateProjectOpenState.value = false
    }

    fun createProject(name: String, description: String) {
        viewModelScope.launch {
            val project = Project(name = name, description = description)
            projectRepository.saveProject(project)
            isCreateProjectOpenState.value = false
        }
    }

    fun openCreateTaskDialog() {
        isCreateTaskOpenState.value = true
    }

    fun closeCreateTaskDialog() {
        isCreateTaskOpenState.value = false
    }

    fun createTask(
        title: String,
        description: String,
        priority: TaskPriority,
        projectId: String?,
        isToday: Boolean
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority,
                projectId = projectId,
                isToday = projectId == null || isToday
            )
            taskRepository.saveTask(task)
            isCreateTaskOpenState.value = false
        }
    }

    fun createNewNote(onNoteCreated: (String) -> Unit) {
        viewModelScope.launch {
            val note = Note(title = "Untitled Note", content = "")
            noteRepository.saveNote(note)
            onNoteCreated(note.id)
        }
    }

    fun launchApp(packageName: String) {
        appLauncherRepository.launchApp(packageName)
    }

    fun launchAppInfo(packageName: String) {
        appLauncherRepository.launchAppInfo(packageName)
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(task)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setAccentColorHex(hex: String) {
        viewModelScope.launch {
            preferencesRepository.setAccentColorHex(hex)
        }
    }

    fun setBrightnessValue(value: Float) {
        viewModelScope.launch {
            preferencesRepository.setBrightnessValue(value)
        }
    }

    fun setContrastValue(value: Float) {
        viewModelScope.launch {
            preferencesRepository.setContrastValue(value)
        }
    }

    fun setSaturationValue(value: Float) {
        viewModelScope.launch {
            preferencesRepository.setSaturationValue(value)
        }
    }

    fun setWallpaperDimValue(value: Float) {
        viewModelScope.launch {
            preferencesRepository.setWallpaperDimValue(value)
        }
    }
}
