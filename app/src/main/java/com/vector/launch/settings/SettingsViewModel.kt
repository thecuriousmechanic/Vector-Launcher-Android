package com.vector.launch.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.launch.core.datastore.PreferencesRepository
import com.vector.launch.core.model.DashboardWidgetConfig
import com.vector.launch.core.model.WidgetType
import com.vector.launch.core.repository.BackupRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.combine

data class SettingsUiState(
    val themeMode: String = "OLED",
    val accentColorHex: String = "#A5B4FC",
    val brightnessValue: Float = 1.0f,
    val contrastValue: Float = 1.0f,
    val saturationValue: Float = 1.0f,
    val wallpaperDimValue: Float = 0.20f,
    val wallpaperUri: String? = null,
    val homeSections: List<DashboardWidgetConfig> = emptyList(),
    val isBackupBusy: Boolean = false,
    val message: String? = null
)

private data class BackupOperationState(
    val isBusy: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val backupOperationState = MutableStateFlow(BackupOperationState())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<SettingsUiState> = combine<Any?, SettingsUiState>(
        preferencesRepository.themeMode,
        preferencesRepository.accentColorHex,
        preferencesRepository.brightnessValue,
        preferencesRepository.contrastValue,
        preferencesRepository.saturationValue,
        preferencesRepository.wallpaperDimValue,
        preferencesRepository.wallpaperUri,
        preferencesRepository.widgetConfigs,
        backupOperationState
    ) { flows: Array<Any?> ->
        val theme = flows[0] as String
        val accent = flows[1] as String
        val brightness = flows[2] as Float
        val contrast = flows[3] as Float
        val saturation = flows[4] as Float
        val dim = flows[5] as Float
        val wallpaper = flows[6] as String?
        val sections = flows[7] as List<DashboardWidgetConfig>
        val backupState = flows[8] as BackupOperationState

        SettingsUiState(
            themeMode = theme,
            accentColorHex = accent,
            brightnessValue = brightness,
            contrastValue = contrast,
            saturationValue = saturation,
            wallpaperDimValue = dim,
            wallpaperUri = wallpaper,
            homeSections = sections.sortedBy(DashboardWidgetConfig::position),
            isBackupBusy = backupState.isBusy,
            message = backupState.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

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

    fun setWallpaperUri(uri: String?) {
        viewModelScope.launch {
            preferencesRepository.setWallpaperUri(uri)
        }
    }

    fun removeWallpaper() {
        viewModelScope.launch {
            preferencesRepository.removeWallpaper()
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            preferencesRepository.savePreferredAppPackages(emptyList())
        }
    }

    fun toggleHomeSection(widgetId: String) {
        viewModelScope.launch {
            val updated = uiState.value.homeSections.map { section ->
                if (section.id == widgetId) section.copy(isVisible = !section.isVisible) else section
            }
            preferencesRepository.saveWidgetConfigs(updated)
        }
    }

    fun moveHomeSection(widgetId: String, direction: Int) {
        if (direction == 0) return
        viewModelScope.launch {
            val current = uiState.value.homeSections.sortedBy(DashboardWidgetConfig::position)
            val clock = current.firstOrNull { it.type == WidgetType.CLOCK }
            val favorites = current.firstOrNull { it.type == WidgetType.APP_LAUNCHER }
            val movable = current.filter {
                it.type != WidgetType.CLOCK && it.type != WidgetType.APP_LAUNCHER
            }.toMutableList()
            val fromIndex = movable.indexOfFirst { it.id == widgetId }
            if (fromIndex < 0) return@launch
            val toIndex = (fromIndex + direction).coerceIn(0, movable.lastIndex)
            if (fromIndex == toIndex) return@launch
            val moved = movable.removeAt(fromIndex)
            movable.add(toIndex, moved)
            val reordered = buildList {
                clock?.let { add(it.copy(position = 0)) }
                addAll(movable.mapIndexed { index, item -> item.copy(position = index + 1) })
                favorites?.let { add(it.copy(position = size)) }
            }
            preferencesRepository.saveWidgetConfigs(reordered)
        }
    }

    fun resetHomeSections() {
        viewModelScope.launch { preferencesRepository.resetWidgetConfigs() }
    }

    fun exportBackup(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            backupOperationState.value = BackupOperationState(isBusy = true)
            val result = runCatching {
                val json = backupRepository.createBackupJson()
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { writer ->
                        writer.write(json)
                    } ?: error("The selected file could not be opened.")
                }
            }
            backupOperationState.value = BackupOperationState(
                message = result.fold(
                    onSuccess = { "Backup exported." },
                    onFailure = { it.message ?: "Backup export failed." }
                )
            )
        }
    }

    fun restoreBackup(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            backupOperationState.value = BackupOperationState(isBusy = true)
            val result = runCatching {
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("The selected file could not be opened.")
                }
                backupRepository.restoreBackupJson(json)
            }
            backupOperationState.value = BackupOperationState(
                message = result.fold(
                    onSuccess = { summary -> "Restored ${summary.totalItemCount} items." },
                    onFailure = { it.message ?: "Backup restore failed." }
                )
            )
        }
    }

    fun clearMessage() {
        backupOperationState.value = backupOperationState.value.copy(message = null)
    }
}
