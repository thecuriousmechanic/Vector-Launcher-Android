package com.vector.launch.core.model

enum class SearchResultType {
    TASK,
    PROJECT,
    NOTE,
    RESOURCE,
    APPLICATION
}

data class SearchResult(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val subtitle: String = "",
    val projectId: String? = null,
    val packageName: String? = null
)
