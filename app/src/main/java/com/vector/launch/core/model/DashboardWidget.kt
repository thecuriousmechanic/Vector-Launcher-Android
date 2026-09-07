package com.vector.launch.core.model

enum class WidgetType {
    APP_LAUNCHER,
    TASKS,
    PROJECTS,
    NOTES,
    CLOCK,
    CALENDAR,
    WEATHER
}

data class DashboardWidgetConfig(
    val id: String,
    val type: WidgetType,
    val title: String,
    val position: Int,
    val isVisible: Boolean = true,
    val isExpanded: Boolean = true
)
