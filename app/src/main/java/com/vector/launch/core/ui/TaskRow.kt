package com.vector.launch.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vector.launch.core.model.Task
import com.vector.launch.core.model.TaskPriority

internal fun taskCompletionMarker(completed: Boolean): String = if (completed) "[✓]" else "[ ]"

@Composable
fun TaskCompletionToggle(
    completed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val actionDescription = if (completed) "Mark task as incomplete" else "Mark task as complete"

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .toggleable(
                value = completed,
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            )
            .semantics {
                contentDescription = actionDescription
                stateDescription = if (completed) "Completed" else "Not completed"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = taskCompletionMarker(completed),
            style = textStyle.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (completed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggleTask: (Task) -> Unit,
    modifier: Modifier = Modifier,
    onSelectTask: ((String) -> Unit)? = null
) {
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFEF4444)   // RED
        TaskPriority.MEDIUM -> Color(0xFFF59E0B) // YELLOW
        TaskPriority.LOW -> Color(0xFF10B981)    // GREEN
        TaskPriority.NONE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val priorityName = when (task.priority) {
        TaskPriority.HIGH -> "High priority"
        TaskPriority.MEDIUM -> "Medium priority"
        TaskPriority.LOW -> "Low priority"
        TaskPriority.NONE -> "Normal task"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .semantics {
                contentDescription = "$priorityName: ${task.title}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority Visual Indicator Bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(priorityColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        TaskCompletionToggle(
            completed = task.completed,
            onToggle = { onToggleTask(task) }
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Task Title
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (task.completed) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onSelectTask != null) {
                        Modifier.clickable { onSelectTask(task.id) }
                    } else {
                        Modifier
                    }
                )
        )
    }
}
