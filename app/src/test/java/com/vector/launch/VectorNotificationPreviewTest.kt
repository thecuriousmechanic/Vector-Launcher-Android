package com.vector.launch

import com.vector.launch.notifications.NotificationPreviewCandidate
import com.vector.launch.notifications.buildNotificationPreviewMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VectorNotificationPreviewTest {

    @Test
    fun newestNotificationBecomesTheVisiblePreview() {
        val previews = buildNotificationPreviewMap(
            listOf(
                NotificationPreviewCandidate("mail.app", "Earlier", "First message", 100L),
                NotificationPreviewCandidate("mail.app", "Newest", "Second message", 300L),
                NotificationPreviewCandidate("chat.app", "Chat", "Hello", 200L)
            )
        )

        assertEquals("Newest", previews.getValue("mail.app").title)
        assertEquals("Second message", previews.getValue("mail.app").text)
        assertEquals(2, previews.getValue("mail.app").count)
        assertEquals(300L, previews.getValue("mail.app").postedAt)
    }

    @Test
    fun emptyNotificationInputProducesNoPreviews() {
        assertTrue(buildNotificationPreviewMap(emptyList()).isEmpty())
    }
}
