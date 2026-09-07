package com.vector.launch.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class VectorNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        publishActiveNotifications()
    }

    override fun onListenerDisconnected() {
        NotificationPreviewStore.clear()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        publishActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        publishActiveNotifications()
    }

    private fun publishActiveNotifications() {
        val candidates = runCatching { activeNotifications.orEmpty() }.getOrDefault(emptyArray())
            .filterNot { it.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0 }
            .map { item ->
                val (title, text) = notificationText(item.notification)
                NotificationPreviewCandidate(
                    packageName = item.packageName,
                    title = title,
                    text = text,
                    postedAt = item.postTime
                )
            }

        NotificationPreviewStore.publish(buildNotificationPreviewMap(candidates))
    }
}
