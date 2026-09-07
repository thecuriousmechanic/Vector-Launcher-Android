package com.vector.launch.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.vector.launch.core.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(
    allApps: List<AppInfo>,
    initialSelectedPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val selectedPackages = remember { mutableStateListOf<String>().apply { addAll(initialSelectedPackages) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedPackages.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = {
            Text(
                text = "Choose favorites",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Box(modifier = Modifier.height(360.dp)) {
                if (allApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(allApps, key = { it.packageName }) { app ->
                            val isSelected = selectedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedPackages.remove(app.packageName)
                                        } else {
                                            selectedPackages.add(app.packageName)
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!selectedPackages.contains(app.packageName)) {
                                                selectedPackages.add(app.packageName)
                                            }
                                        } else {
                                            selectedPackages.remove(app.packageName)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                app.icon?.let { iconDrawable ->
                                    val bitmap = remember(app.packageName) {
                                        iconDrawable.toBitmap(width = 36, height = 36).asImageBitmap()
                                    }
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = app.label,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
