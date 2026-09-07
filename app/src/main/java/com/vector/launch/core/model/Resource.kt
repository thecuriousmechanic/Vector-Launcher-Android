package com.vector.launch.core.model

import java.util.UUID

enum class ResourceType {
    LINK,
    DOC,
    PDF,
    IMAGE,
    OTHER
}

data class Resource(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uri: String = "",
    val type: ResourceType = ResourceType.LINK,
    val projectId: String? = null,
    val taskId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
