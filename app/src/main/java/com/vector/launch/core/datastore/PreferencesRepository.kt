package com.vector.launch.core.datastore

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vector.launch.core.model.DashboardWidgetConfig
import com.vector.launch.core.model.WidgetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

import androidx.datastore.preferences.core.floatPreferencesKey

private val Context.preferencesDataStore by preferencesDataStore(name = "vector_preferences")

data class PreferencesSnapshot(
    val themeMode: String,
    val accentColorHex: String,
    val brightnessValue: Float,
    val contrastValue: Float,
    val saturationValue: Float,
    val wallpaperDimValue: Float,
    val preferredAppPackages: List<String>,
    val widgetConfigs: List<DashboardWidgetConfig>
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val BRIGHTNESS_VAL = floatPreferencesKey("brightness_val")
        val CONTRAST_VAL = floatPreferencesKey("contrast_val")
        val SATURATION_VAL = floatPreferencesKey("saturation_val")
        val WALLPAPER_DIM_VAL = floatPreferencesKey("wallpaper_dim_val")
        val PREFERRED_APP_PACKAGES_JSON = stringPreferencesKey("preferred_app_packages_json")
        val WIDGET_LAYOUT_JSON = stringPreferencesKey("widget_layout_json")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
    }

    val themeMode: Flow<String> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "OLED"
    }

    val accentColorHex: Flow<String> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.ACCENT_COLOR_HEX] ?: "#A5B4FC"
    }

    val brightnessValue: Flow<Float> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.BRIGHTNESS_VAL] ?: 1.0f
    }

    val contrastValue: Flow<Float> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.CONTRAST_VAL] ?: 1.0f
    }

    val saturationValue: Flow<Float> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.SATURATION_VAL] ?: 1.0f
    }

    val wallpaperDimValue: Flow<Float> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.WALLPAPER_DIM_VAL] ?: 0.20f
    }

    val wallpaperUri: Flow<String?> = context.preferencesDataStore.data.map { prefs ->
        prefs[Keys.WALLPAPER_URI]
    }

    val preferredAppPackages: Flow<List<String>> = context.preferencesDataStore.data.map { prefs ->
        val json = prefs[Keys.PREFERRED_APP_PACKAGES_JSON]
        if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    val widgetConfigs: Flow<List<DashboardWidgetConfig>> = context.preferencesDataStore.data.map { prefs ->
        val json = prefs[Keys.WIDGET_LAYOUT_JSON]
        if (json.isNullOrBlank()) {
            getDefaultWidgetConfigs()
        } else {
            parseWidgetConfigs(json)
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setAccentColorHex(hex: String) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.ACCENT_COLOR_HEX] = hex
        }
    }

    suspend fun setBrightnessValue(value: Float) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.BRIGHTNESS_VAL] = value.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun setContrastValue(value: Float) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.CONTRAST_VAL] = value.coerceIn(0.5f, 1.5f)
        }
    }

    suspend fun setSaturationValue(value: Float) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.SATURATION_VAL] = value.coerceIn(0.0f, 2.0f)
        }
    }

    suspend fun setWallpaperDimValue(value: Float) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.WALLPAPER_DIM_VAL] = value.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun setWallpaperUri(uri: String?) {
        val previousUri = context.preferencesDataStore.data.first()[Keys.WALLPAPER_URI]
        context.preferencesDataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(Keys.WALLPAPER_URI)
            } else {
                prefs[Keys.WALLPAPER_URI] = uri
            }
        }
        if (!previousUri.isNullOrBlank() && previousUri != uri) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    previousUri.toUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
    }

    suspend fun removeWallpaper() {
        setWallpaperUri(null)
    }

    suspend fun savePreferredAppPackages(packages: List<String>) {
        val jsonArray = JSONArray()
        packages.forEach { jsonArray.put(it) }
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.PREFERRED_APP_PACKAGES_JSON] = jsonArray.toString()
        }
    }

    suspend fun createSnapshot(): PreferencesSnapshot = PreferencesSnapshot(
        themeMode = themeMode.first(),
        accentColorHex = accentColorHex.first(),
        brightnessValue = brightnessValue.first(),
        contrastValue = contrastValue.first(),
        saturationValue = saturationValue.first(),
        wallpaperDimValue = wallpaperDimValue.first(),
        preferredAppPackages = preferredAppPackages.first(),
        widgetConfigs = widgetConfigs.first()
    )

    suspend fun restoreSnapshot(snapshot: PreferencesSnapshot) {
        val favoritesJson = JSONArray().apply {
            snapshot.preferredAppPackages.distinct().forEach(::put)
        }
        val widgetLayoutJson = widgetConfigsToJson(snapshot.widgetConfigs)
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = snapshot.themeMode
            prefs[Keys.ACCENT_COLOR_HEX] = snapshot.accentColorHex
            prefs[Keys.BRIGHTNESS_VAL] = snapshot.brightnessValue.coerceIn(0f, 1f)
            prefs[Keys.CONTRAST_VAL] = snapshot.contrastValue.coerceIn(0.5f, 1.5f)
            prefs[Keys.SATURATION_VAL] = snapshot.saturationValue.coerceIn(0f, 2f)
            prefs[Keys.WALLPAPER_DIM_VAL] = snapshot.wallpaperDimValue.coerceIn(0f, 1f)
            prefs[Keys.PREFERRED_APP_PACKAGES_JSON] = favoritesJson.toString()
            prefs[Keys.WIDGET_LAYOUT_JSON] = widgetLayoutJson
        }
    }

    suspend fun saveWidgetConfigs(configs: List<DashboardWidgetConfig>) {
        context.preferencesDataStore.edit { prefs ->
            prefs[Keys.WIDGET_LAYOUT_JSON] = widgetConfigsToJson(configs)
        }
    }

    suspend fun resetWidgetConfigs() {
        saveWidgetConfigs(getDefaultWidgetConfigs())
    }

    companion object {
        fun getDefaultWidgetConfigs(): List<DashboardWidgetConfig> {
            return listOf(
                DashboardWidgetConfig("w_clock", WidgetType.CLOCK, "Time & Date", 0, isVisible = true),
                DashboardWidgetConfig("w_tasks", WidgetType.TASKS, "Today / Priority", 1, isVisible = true),
                DashboardWidgetConfig("w_projects", WidgetType.PROJECTS, "Projects", 2, isVisible = true),
                DashboardWidgetConfig("w_notes", WidgetType.NOTES, "Notes", 3, isVisible = true),
                DashboardWidgetConfig("w_app_launcher", WidgetType.APP_LAUNCHER, "Favorites", 4, isVisible = true)
            )
        }

        private fun parseWidgetConfigs(jsonStr: String): List<DashboardWidgetConfig> {
            return try {
                val array = JSONArray(jsonStr)
                val defaults = getDefaultWidgetConfigs()
                val supportedTypes = defaults.mapTo(hashSetOf(), DashboardWidgetConfig::type)
                val list = mutableListOf<DashboardWidgetConfig>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val typeStr = obj.getString("type")
                    val id = obj.getString("id")
                    if (id == "w_capture") continue
                    val type = runCatching { WidgetType.valueOf(typeStr) }.getOrNull() ?: continue
                    if (type !in supportedTypes) continue
                    list.add(
                        DashboardWidgetConfig(
                            id = id,
                            type = type,
                            title = obj.getString("title"),
                            position = obj.optInt("position", i),
                            isVisible = obj.optBoolean("isVisible", true),
                            isExpanded = obj.optBoolean("isExpanded", true)
                        )
                    )
                }
                val completed = list.distinctBy(DashboardWidgetConfig::type).toMutableList()
                defaults.forEach { default ->
                    if (completed.none { it.type == default.type }) completed += default
                }
                normalizeWidgetConfigs(completed)
            } catch (e: Exception) {
                getDefaultWidgetConfigs()
            }
        }

        private fun widgetConfigsToJson(configs: List<DashboardWidgetConfig>): String =
            JSONArray().apply {
                normalizeWidgetConfigs(configs).forEachIndexed { position, config ->
                    put(JSONObject().apply {
                        put("id", config.id)
                        put("type", config.type.name)
                        put("title", config.title)
                        put("position", position)
                        put("isVisible", config.isVisible)
                        put("isExpanded", config.isExpanded)
                    })
                }
            }.toString()

        private fun normalizeWidgetConfigs(
            configs: List<DashboardWidgetConfig>
        ): List<DashboardWidgetConfig> {
            val unique = configs.distinctBy(DashboardWidgetConfig::type)
            val clock = unique.firstOrNull { it.type == WidgetType.CLOCK }
            val favorites = unique.firstOrNull { it.type == WidgetType.APP_LAUNCHER }
            val middle = unique
                .filter { it.type != WidgetType.CLOCK && it.type != WidgetType.APP_LAUNCHER }
                .sortedBy(DashboardWidgetConfig::position)
            return buildList {
                clock?.let(::add)
                addAll(middle)
                favorites?.let(::add)
            }.mapIndexed { position, config -> config.copy(position = position) }
        }
    }
}
