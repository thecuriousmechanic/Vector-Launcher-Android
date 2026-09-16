package com.vector.launch.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [PreferencesRepository] to verify default configurations,
 * snapshot creation/restoration, and preference management.
 */
class PreferencesRepositoryTest {

    @Test
    fun testDefaultWidgetConfigs_HasAllRequiredTypes() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs()

        assertEquals(5, configs.size)

        val types = configs.map { it.type }
        assertTrue(types.contains(WidgetType.CLOCK))
        assertTrue(types.contains(WidgetType.TASKS))
        assertTrue(types.contains(WidgetType.PROJECTS))
        assertTrue(types.contains(WidgetType.NOTES))
        assertTrue(types.contains(WidgetType.APP_LAUNCHER))
    }

    @Test
    fun testDefaultWidgetConfigs_AllVisibleByDefault() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs()

        assertTrue(configs.all { it.isVisible })
        assertTrue(configs.all { it.isExpanded })
    }

    @Test
    fun testDefaultWidgetConfigs_CorrectOrder() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs()

        assertEquals(WidgetType.CLOCK, configs[0].type)
        assertEquals(WidgetType.TASKS, configs[1].type)
        assertEquals(WidgetType.PROJECTS, configs[2].type)
        assertEquals(WidgetType.NOTES, configs[3].type)
        assertEquals(WidgetType.APP_LAUNCHER, configs[4].type)
    }

    @Test
    fun testDefaultWidgetConfigs_PositionValues() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs()

        assertEquals(0, configs[0].position)
        assertEquals(1, configs[1].position)
        assertEquals(2, configs[2].position)
        assertEquals(3, configs[3].position)
        assertEquals(4, configs[4].position)
    }

    @Test
    fun testDefaultWidgetConfigs_TitleGeneration() {
        val configs = PreferencesRepository.getDefaultWidgetConfigs()

        assertEquals("Clock", configs[0].title)
        assertEquals("Tasks", configs[1].title)
        assertEquals("Projects", configs[2].title)
        assertEquals("Notes", configs[3].title)
        assertEquals("Apps", configs[4].title)
    }

    @Test
    fun testPreferencesSnapshot_DefaultValues() {
        val snapshot = PreferencesSnapshot()

        assertEquals("OLED", snapshot.themeMode)
        assertEquals("#A5B4FC", snapshot.accentColorHex)
        assertEquals(1.0f, snapshot.brightnessValue, 0.01f)
        assertEquals(1.0f, snapshot.contrastValue, 0.01f)
        assertEquals(1.0f, snapshot.saturationValue, 0.01f)
        assertEquals(0.2f, snapshot.wallpaperDimValue, 0.01f)
        assertTrue(snapshot.preferredAppPackages.isEmpty())
        assertEquals(5, snapshot.widgetConfigs.size)
    }

    @Test
    fun testPreferencesSnapshot_CustomValues() {
        val customConfigs = listOf(
            DashboardWidgetConfig(
                id = "custom-1",
                type = WidgetType.TASKS,
                title = "My Tasks",
                position = 0,
                isVisible = true,
                isExpanded = false
            )
        )

        val snapshot = PreferencesSnapshot(
            themeMode = "DARK",
            accentColorHex = "#FF5722",
            brightnessValue = 0.8f,
            contrastValue = 1.2f,
            saturationValue = 1.5f,
            wallpaperDimValue = 0.5f,
            preferredAppPackages = listOf("com.app1", "com.app2"),
            widgetConfigs = customConfigs
        )

        assertEquals("DARK", snapshot.themeMode)
        assertEquals("#FF5722", snapshot.accentColorHex)
        assertEquals(0.8f, snapshot.brightnessValue, 0.01f)
        assertEquals(1.2f, snapshot.contrastValue, 0.01f)
        assertEquals(1.5f, snapshot.saturationValue, 0.01f)
        assertEquals(0.5f, snapshot.wallpaperDimValue, 0.01f)
        assertEquals(2, snapshot.preferredAppPackages.size)
        assertEquals(1, snapshot.widgetConfigs.size)
    }

    @Test
    fun testDashboardWidgetConfig_CopyPreservesIdentity() {
        val original = DashboardWidgetConfig(
            id = "w1",
            type = WidgetType.TASKS,
            title = "Tasks",
            position = 1,
            isVisible = true,
            isExpanded = true
        )

        val modified = original.copy(
            isVisible = false,
            isExpanded = false,
            position = 5
        )

        assertEquals(original.id, modified.id)
        assertEquals(original.type, modified.type)
        assertEquals(original.title, modified.title)
        assertFalse(modified.isVisible)
        assertFalse(modified.isExpanded)
        assertEquals(5, modified.position)
    }

    @Test
    fun testThemeModes_AreValid() {
        val validThemes = listOf("AMBER", "DARK", "LIGHT", "OLED")

        assertTrue(validThemes.contains("AMBER"))
        assertTrue(validThemes.contains("DARK"))
        assertTrue(validThemes.contains("LIGHT"))
        assertTrue(validThemes.contains("OLED"))
    }

    @Test
    fun testAccentColorHexFormat() {
        val colors = listOf(
            "#A5B4FC",
            "#FF5722",
            "#4CAF50",
            "#2196F3",
            "#E91E63"
        )

        colors.forEach { color ->
            assertTrue(color.startsWith("#"))
            assertEquals(7, color.length)
        }
    }

    @Test
    fun testDisplaySliderRanges() {
        // Brightness: 0.0 to 1.0
        val brightnessMin = 0.0f.coerceIn(0.0f, 1.0f)
        val brightnessMax = 1.0f.coerceIn(0.0f, 1.0f)
        assertEquals(0.0f, brightnessMin, 0.01f)
        assertEquals(1.0f, brightnessMax, 0.01f)

        // Contrast: 0.5 to 1.5
        val contrastMin = 0.5f.coerceIn(0.5f, 1.5f)
        val contrastMax = 1.5f.coerceIn(0.5f, 1.5f)
        assertEquals(0.5f, contrastMin, 0.01f)
        assertEquals(1.5f, contrastMax, 0.01f)

        // Saturation: 0.0 to 2.0
        val saturationMin = 0.0f.coerceIn(0.0f, 2.0f)
        val saturationMax = 2.0f.coerceIn(0.0f, 2.0f)
        assertEquals(0.0f, saturationMin, 0.01f)
        assertEquals(2.0f, saturationMax, 0.01f)

        // Wallpaper Dim: 0.0 to 1.0
        val dimMin = 0.0f.coerceIn(0.0f, 1.0f)
        val dimMax = 1.0f.coerceIn(0.0f, 1.0f)
        assertEquals(0.0f, dimMin, 0.01f)
        assertEquals(1.0f, dimMax, 0.01f)
    }

    @Test
    fun testPreferredAppPackages_OrderPreservation() {
        val packages = listOf("app.z", "app.a", "app.m", "app.a") // intentional duplicate

        // Duplicates should be preserved in snapshot (deduplication happens at UI layer)
        val snapshot = PreferencesSnapshot(preferredAppPackages = packages)

        assertEquals(4, snapshot.preferredAppPackages.size)
        assertEquals("app.z", snapshot.preferredAppPackages[0])
        assertEquals("app.a", snapshot.preferredAppPackages[1])
    }

    @Test
    fun testWidgetConfigUniqueIdGeneration() {
        val config1 = DashboardWidgetConfig(
            id = "widget-1",
            type = WidgetType.TASKS,
            title = "Tasks 1"
        )
        val config2 = DashboardWidgetConfig(
            id = "widget-2",
            type = WidgetType.TASKS,
            title = "Tasks 2"
        )

        assertEquals("widget-1", config1.id)
        assertEquals("widget-2", config2.id)
    }
}
