package com.vector.launch.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.vector.launch.core.model.AppInfo
import com.vector.launch.notifications.AppNotificationPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal sealed class AppBrowserListItem {
    data class Header(val letter: Char) : AppBrowserListItem()
    data class AppRow(val app: AppInfo) : AppBrowserListItem()
}

internal data class AppBrowserListModel(
    val items: List<AppBrowserListItem>,
    val sectionIndices: Map<Char, Int>
)

internal fun buildAppBrowserListModel(allApps: List<AppInfo>): AppBrowserListModel {
    val items = mutableListOf<AppBrowserListItem>()
    val sectionIndices = mutableMapOf<Char, Int>()
    val appsByLetter = allApps
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        .groupBy(AppInfo::firstLetter)

    APP_ALPHABET.forEach { letter ->
        val apps = appsByLetter[letter].orEmpty()
        if (apps.isNotEmpty()) {
            sectionIndices[letter] = items.size
            items += AppBrowserListItem.Header(letter)
            apps.forEach { items += AppBrowserListItem.AppRow(it) }
        }
    }

    return AppBrowserListModel(items, sectionIndices)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppBrowserSheet(
    allApps: List<AppInfo>,
    requestedLetter: Char? = null,
    notificationPreviews: Map<String, AppNotificationPreview> = emptyMap(),
    onDismiss: () -> Unit,
    onShowFavorites: () -> Unit = onDismiss,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppInfo) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var scrubScrollJob by remember { mutableStateOf<Job?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val filteredApps = remember(allApps, query) {
        if (query.isBlank()) allApps else allApps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }
    val listModel = remember(filteredApps) {
        buildAppBrowserListModel(filteredApps)
    }
    val listItems = listModel.items
    val letterIndexMap = listModel.sectionIndices

    BackHandler(onBack = onDismiss)

    LaunchedEffect(requestedLetter, letterIndexMap) {
        if (requestedLetter == null) {
            return@LaunchedEffect
        }

        findNearestAlphabetSectionIndex(requestedLetter, letterIndexMap)?.let { targetIndex ->
            listState.scrollToItem(targetIndex)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Apps",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 64.dp, bottom = 8.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // App List Column
                Box(modifier = Modifier.weight(1f)) {
                    if (filteredApps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (query.isBlank()) "No apps found." else "No apps match “$query”.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            itemsIndexed(
                                items = listItems,
                                key = { _, item ->
                                    when (item) {
                                        is AppBrowserListItem.Header -> "header_${item.letter}"
                                        is AppBrowserListItem.AppRow -> "app_${item.app.packageName}"
                                    }
                                }
                            ) { _, item ->
                                when (item) {
                                    is AppBrowserListItem.Header -> {
                                        Text(
                                            text = item.letter.toString(),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }

                                    is AppBrowserListItem.AppRow -> {
                                        val app = item.app
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 56.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .combinedClickable(
                                                    onClick = {
                                                        onLaunchApp(app.packageName)
                                                        onDismiss()
                                                    },
                                                    onLongClick = { onLongPressApp(app) }
                                                )
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (app.icon != null) {
                                                val bitmap = remember(app.packageName) {
                                                    app.icon.toBitmap(width = 40, height = 40).asImageBitmap()
                                                }
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = app.label,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = app.label.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = app.label,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                notificationPreviews[app.packageName]?.let { preview ->
                                                    val previewText = listOf(preview.title, preview.text)
                                                        .filter(String::isNotBlank)
                                                        .joinToString(" · ")
                                                    if (previewText.isNotBlank()) {
                                                        Text(
                                                            text = previewText,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Continuous Drag Wave Alphabet Index Bar
                WaveAlphabetIndexBar(
                    items = APP_NAVIGATION_ITEMS,
                    availableItems = letterIndexMap.keys + FAVORITES_ITEM,
                    onScrubLetter = { targetChar ->
                        if (targetChar == FAVORITES_ITEM) {
                            onShowFavorites()
                        } else {
                            val targetIndex = letterIndexMap[targetChar]
                                ?: findNearestAlphabetSectionIndex(targetChar, letterIndexMap)
                            if (targetIndex != null) {
                                scrubScrollJob?.cancel()
                                scrubScrollJob = coroutineScope.launch { listState.scrollToItem(targetIndex) }
                            }
                        }
                    },
                    onScrubEnd = {
                        scrubScrollJob?.cancel()
                    }
                )
            }
        }
    }
}
