package com.vector.launch.notifications

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppNotificationPreview(
    val packageName: String,
    val title: String,
    val text: String,
    val count: Int,
    val postedAt: Long
)

internal data class NotificationPreviewCandidate(
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long
)

internal fun buildNotificationPreviewMap(
    candidates: List<NotificationPreviewCandidate>
): Map<String, AppNotificationPreview> = candidates
    .sortedByDescending(NotificationPreviewCandidate::postedAt)
    .groupBy(NotificationPreviewCandidate::packageName)
    .mapValues { (packageName, items) ->
        val newest = items.first()
        AppNotificationPreview(
            packageName = packageName,
            title = newest.title,
            text = newest.text,
            count = items.size,
            postedAt = newest.postedAt
        )
    }

object NotificationPreviewStore {
    private val mutablePreviews = MutableStateFlow<Map<String, AppNotificationPreview>>(emptyMap())
    val previews: StateFlow<Map<String, AppNotificationPreview>> = mutablePreviews.asStateFlow()

    internal fun publish(previews: Map<String, AppNotificationPreview>) {
        mutablePreviews.value = previews
    }

    internal fun clear() {
        mutablePreviews.value = emptyMap()
    }
}

fun hasNotificationPreviewAccess(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

fun openNotificationPreviewSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

internal fun notificationText(notification: Notification): Pair<String, String> {
    val extras = notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
        ?: extras.getCharSequence(Notification.EXTRA_TITLE)
        ?: "Notification"
    val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
        ?: extras.getCharSequence(Notification.EXTRA_TEXT)
        ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        ?: ""
    return title.toString().trim() to text.toString().trim()
}
