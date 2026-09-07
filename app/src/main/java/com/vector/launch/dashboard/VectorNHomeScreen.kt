package com.vector.launch.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.launch.core.model.AppInfo
import com.vector.launch.core.model.DashboardWidgetConfig
import com.vector.launch.core.model.Note
import com.vector.launch.core.model.Project
import com.vector.launch.core.model.Task
import com.vector.launch.core.model.WidgetType
import com.vector.launch.core.ui.DisplayControlUtils
import com.vector.launch.core.ui.TaskRow
import com.vector.launch.core.ui.WallpaperUtils
import com.vector.launch.launcher.APP_NAVIGATION_ITEMS
import com.vector.launch.launcher.FAVORITES_ITEM
import com.vector.launch.launcher.AppBrowserSheet
import com.vector.launch.launcher.AppContextMenu
import com.vector.launch.launcher.AppPickerSheet
import com.vector.launch.launcher.WaveAlphabetIndexBar
import com.vector.launch.notifications.AppNotificationPreview
import com.vector.launch.notifications.NotificationPreviewStore
import com.vector.launch.projects.CreateProjectDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class VectorSpace(val title: String) {
    TASKS("Today"),
    PROJECTS("Projects"),
    NOTES("Notes")
}

internal fun orderedHomeContentSections(
    configs: List<DashboardWidgetConfig>
): List<DashboardWidgetConfig> {
    val visible = configs.filter(DashboardWidgetConfig::isVisible)
    val workSections = visible
        .filter { it.type in setOf(WidgetType.TASKS, WidgetType.PROJECTS, WidgetType.NOTES) }
        .sortedBy(DashboardWidgetConfig::position)
    val favorites = visible.firstOrNull { it.type == WidgetType.APP_LAUNCHER }
    return if (favorites == null) workSections else workSections + favorites
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VectorNHomeScreen(
    viewModel: DashboardViewModel,
    onNavigateToTask: (taskId: String) -> Unit,
    onNavigateToProject: (projectId: String) -> Unit,
    onNavigateToNote: (noteId: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationPreviews by NotificationPreviewStore.previews.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedSpace by remember { mutableStateOf<VectorSpace?>(null) }
    var isQuickCaptureOpen by remember { mutableStateOf(false) }

    val installedAppLetters = remember(uiState.allInstalledApps) {
        uiState.allInstalledApps.mapTo(mutableSetOf()) { it.firstLetter }
    }
    val visibleHomeSections = remember(uiState.widgetConfigs) {
        orderedHomeContentSections(uiState.widgetConfigs)
    }
    val showClock = uiState.widgetConfigs.firstOrNull { it.type == WidgetType.CLOCK }?.isVisible != false
    val wallpaperBitmap by produceState<Bitmap?>(initialValue = null, key1 = uiState.wallpaperUri) {
        value = uiState.wallpaperUri?.takeIf(String::isNotBlank)?.let { uri ->
            WallpaperUtils.loadDownsampledWallpaper(context, uri, reqWidth = 1080, reqHeight = 1920)
        }
    }

    if (uiState.isAppPickerOpen) {
        AppPickerSheet(
            allApps = uiState.allInstalledApps,
            initialSelectedPackages = uiState.preferredAppPackages,
            onDismiss = viewModel::closeAppPicker,
            onConfirm = viewModel::savePreferredAppPackages
        )
    }

    uiState.selectedContextApp?.let { app ->
        AppContextMenu(
            app = app,
            isFavorite = app.packageName in uiState.preferredAppPackages,
            onDismiss = viewModel::closeAppContextMenu,
            onOpenApp = { viewModel.launchApp(app.packageName) },
            onToggleFavorite = { viewModel.toggleFavoriteApp(app.packageName) },
            onOpenAppInfo = { viewModel.launchAppInfo(app.packageName) }
        )
    }

    if (uiState.isCreateProjectOpen) {
        CreateProjectDialog(
            onDismiss = viewModel::closeCreateProjectDialog,
            onCreateProject = viewModel::createProject
        )
    }

    if (uiState.isCreateTaskOpen) {
        CreateTaskDialog(
            projects = uiState.projects,
            onDismiss = viewModel::closeCreateTaskDialog,
            onCreateTask = viewModel::createTask
        )
    }

    val displayColorFilter = remember(
        uiState.brightnessValue,
        uiState.contrastValue,
        uiState.saturationValue
    ) {
        DisplayControlUtils.buildDisplayColorFilter(
            brightness = uiState.brightnessValue,
            contrast = uiState.contrastValue,
            saturation = uiState.saturationValue
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer { colorFilter = displayColorFilter }
    ) {
        wallpaperBitmap?.let { wallpaper ->
            Image(
                bitmap = wallpaper.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = uiState.wallpaperDimValue.coerceIn(0f, 1f)))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 28.dp, end = 4.dp, top = 18.dp, bottom = 12.dp)
            ) {
                VectorHeader(showClock = showClock, onSearch = onNavigateToSearch)

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    visibleHomeSections.forEachIndexed { index, section ->
                        when (section.type) {
                            WidgetType.TASKS -> TodayHomeSection(
                                tasks = uiState.activeTasks,
                                onAdd = viewModel::openCreateTaskDialog,
                                onViewAll = { selectedSpace = VectorSpace.TASKS },
                                onToggleTask = viewModel::toggleTaskCompleted,
                                onSelectTask = onNavigateToTask,
                                modifier = Modifier.weight(1f)
                            )

                            WidgetType.PROJECTS -> ProjectsHomeSection(
                                projects = uiState.projects,
                                onAdd = viewModel::openCreateProjectDialog,
                                onViewAll = { selectedSpace = VectorSpace.PROJECTS },
                                onSelectProject = onNavigateToProject,
                                modifier = Modifier.weight(1f)
                            )

                            WidgetType.NOTES -> NotesHomeSection(
                                notes = uiState.recentNotes,
                                onAdd = { viewModel.createNewNote(onNavigateToNote) },
                                onViewAll = { selectedSpace = VectorSpace.NOTES },
                                onSelectNote = onNavigateToNote,
                                modifier = Modifier.weight(1f)
                            )

                            WidgetType.APP_LAUNCHER -> FavoriteAppsHomeSection(
                                apps = uiState.preferredApps,
                                notifications = notificationPreviews,
                                onOpenApp = viewModel::launchApp,
                                onLongPressApp = viewModel::openAppContextMenu,
                                onChooseFavorites = viewModel::openAppPicker,
                                modifier = Modifier.weight(1f)
                            )

                            else -> Unit
                        }
                        if (index < visibleHomeSections.lastIndex) {
                            HomeDivider()
                        }
                    }

                    if (visibleHomeSections.isEmpty()) {
                        HomeSectionEmptyState("Your home sections are hidden. Restore them in Settings.")
                    }
                }

                HomeActions(
                    onApps = viewModel::openAppBrowser,
                    onCapture = { isQuickCaptureOpen = true },
                    onSettings = onNavigateToSettings
                )
            }

            WaveAlphabetIndexBar(
                items = APP_NAVIGATION_ITEMS,
                availableItems = installedAppLetters + FAVORITES_ITEM,
                onScrubLetter = { item ->
                    if (item == FAVORITES_ITEM) {
                        viewModel.closeAppBrowser()
                    } else {
                        viewModel.scrubAppBrowserTo(item)
                    }
                },
                onScrubEnd = viewModel::finishAppBrowserScrub
            )
        }

        if (uiState.isAppBrowserOpen) {
            AppBrowserSheet(
                allApps = uiState.allInstalledApps,
                requestedLetter = uiState.appBrowserTargetLetter,
                notificationPreviews = notificationPreviews,
                onDismiss = viewModel::closeAppBrowser,
                onShowFavorites = viewModel::closeAppBrowser,
                onLaunchApp = viewModel::launchApp,
                onLongPressApp = viewModel::openAppContextMenu
            )
        }
    }

    if (isQuickCaptureOpen) {
        QuickCaptureSheet(
            onDismiss = { isQuickCaptureOpen = false },
            onTask = {
                isQuickCaptureOpen = false
                viewModel.openCreateTaskDialog()
            },
            onProject = {
                isQuickCaptureOpen = false
                viewModel.openCreateProjectDialog()
            },
            onNote = {
                isQuickCaptureOpen = false
                viewModel.createNewNote(onNavigateToNote)
            }
        )
    }

    selectedSpace?.let { space ->
        VectorSpaceSheet(
            space = space,
            tasks = uiState.activeTasks,
            projects = uiState.projects,
            notes = uiState.recentNotes,
            onDismiss = { selectedSpace = null },
            onAdd = {
                selectedSpace = null
                when (space) {
                    VectorSpace.TASKS -> viewModel.openCreateTaskDialog()
                    VectorSpace.PROJECTS -> viewModel.openCreateProjectDialog()
                    VectorSpace.NOTES -> viewModel.createNewNote(onNavigateToNote)
                }
            },
            onToggleTask = viewModel::toggleTaskCompleted,
            onTaskSelected = {
                selectedSpace = null
                onNavigateToTask(it)
            },
            onProjectSelected = {
                selectedSpace = null
                onNavigateToProject(it)
            },
            onNoteSelected = {
                selectedSpace = null
                onNavigateToNote(it)
            }
        )
    }
}

@Composable
private fun VectorHeader(
    showClock: Boolean,
    onSearch: () -> Unit
) {
    var now by remember { mutableStateOf(Date()) }
    val context = LocalContext.current

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                now = Date()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val is24Hour = DateFormat.is24HourFormat(context)
    val timeFormat = remember(is24Hour) { SimpleDateFormat(if (is24Hour) "HH:mm" else "h:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        if (showClock) {
            Column {
                Text(
                    text = timeFormat.format(now),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = dateFormat.format(now),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }

        IconButton(onClick = onSearch) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search apps and Vector content",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun TodayHomeSection(
    tasks: List<Task>,
    onAdd: () -> Unit,
    onViewAll: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onSelectTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader("Today", tasks.size, onAdd, onViewAll)
        HomeSectionViewport(modifier = Modifier.weight(1f)) {
            if (tasks.isEmpty()) {
                HomeSectionEmptyState("Nothing is asking for your attention.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(end = 4.dp, bottom = 4.dp)
                ) {
                    items(tasks, key = Task::id) { task ->
                        TaskRow(
                            task = task,
                            onToggleTask = onToggleTask,
                            onSelectTask = onSelectTask
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectsHomeSection(
    projects: List<Project>,
    onAdd: () -> Unit,
    onViewAll: () -> Unit,
    onSelectProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader("Projects", projects.size, onAdd, onViewAll)
        HomeSectionViewport(modifier = Modifier.weight(1f)) {
            if (projects.isEmpty()) {
                HomeSectionEmptyState("Create a project when work needs a home.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(end = 4.dp, bottom = 4.dp)
                ) {
                    items(projects, key = Project::id) { project ->
                        SpaceTextRow(
                            title = project.name,
                            subtitle = project.description,
                            onClick = { onSelectProject(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesHomeSection(
    notes: List<Note>,
    onAdd: () -> Unit,
    onViewAll: () -> Unit,
    onSelectNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader("Notes", notes.size, onAdd, onViewAll)
        HomeSectionViewport(modifier = Modifier.weight(1f)) {
            if (notes.isEmpty()) {
                HomeSectionEmptyState("Keep only the notes worth returning to.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(end = 4.dp, bottom = 4.dp)
                ) {
                    items(notes, key = Note::id) { note ->
                        SpaceTextRow(
                            title = note.title.ifBlank { "Untitled note" },
                            subtitle = note.content,
                            onClick = { onSelectNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteAppsHomeSection(
    apps: List<AppInfo>,
    notifications: Map<String, AppNotificationPreview>,
    onOpenApp: (String) -> Unit,
    onLongPressApp: (AppInfo) -> Unit,
    onChooseFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FavoriteSectionHeader(onEdit = onChooseFavorites)
        HomeSectionViewport(modifier = Modifier.weight(1f)) {
            FavoriteApps(
                apps = apps,
                notifications = notifications,
                onOpenApp = onOpenApp,
                onLongPressApp = onLongPressApp,
                onChooseFavorites = onChooseFavorites,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun HomeSectionViewport(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun HomeSectionEmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        HomeEmptyState(message)
    }
}

@Composable
private fun FavoriteApps(
    apps: List<AppInfo>,
    notifications: Map<String, AppNotificationPreview>,
    onOpenApp: (String) -> Unit,
    onLongPressApp: (AppInfo) -> Unit,
    onChooseFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Choose only the apps you reach for deliberately.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = onChooseFavorites,
            ) {
                Text("Choose favorites")
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(end = 4.dp, bottom = 4.dp)
    ) {
        items(apps, key = AppInfo::packageName) { app ->
            FavoriteAppRow(
                app = app,
                notification = notifications[app.packageName],
                onClick = { onOpenApp(app.packageName) },
                onLongClick = { onLongPressApp(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteAppRow(
    app: AppInfo,
    notification: AppNotificationPreview?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (notification == null) 56.dp else 68.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app)
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (notification != null) {
                    Spacer(modifier = Modifier.width(7.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            notification?.let { preview ->
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
        if ((notification?.count ?: 0) > 1) {
            Text(
                text = notification?.count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun AppIcon(app: AppInfo) {
    val icon = app.icon
    if (icon != null) {
        val bitmap = remember(app.packageName, icon) {
            icon.toBitmap(width = 48, height = 48).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(38.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.label.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    count: Int,
    onAdd: () -> Unit,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onViewAll) {
            Text("All")
        }
        IconButton(onClick = onAdd) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add ${title.lowercase()}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FavoriteSectionHeader(onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "FAVORITE APPS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit")
        }
    }
}

@Composable
private fun HomeEmptyState(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
    )
}

@Composable
private fun HomeDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    )
}

@Composable
private fun HomeActions(
    onApps: () -> Unit,
    onCapture: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onApps) {
            Text("Apps", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onCapture) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Capture", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onSettings) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Settings", fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickCaptureSheet(
    onDismiss: () -> Unit,
    onTask: () -> Unit,
    onProject: () -> Unit,
    onNote: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Capture",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Choose where this thought belongs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            QuickCaptureOption("Task", "Something you need to do", onTask)
            QuickCaptureOption("Project", "Work that needs its own context", onProject)
            QuickCaptureOption("Note", "Something worth remembering", onNote)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuickCaptureOption(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 13.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VectorSpaceSheet(
    space: VectorSpace,
    tasks: List<Task>,
    projects: List<Project>,
    notes: List<Note>,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onTaskSelected: (String) -> Unit,
    onProjectSelected: (String) -> Unit,
    onNoteSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Vector space",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = space.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add ${space.title.lowercase()}")
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 14.dp))

        when (space) {
            VectorSpace.TASKS -> SpaceTaskList(tasks, onToggleTask, onTaskSelected)
            VectorSpace.PROJECTS -> SpaceProjectList(projects, onProjectSelected)
            VectorSpace.NOTES -> SpaceNoteList(notes, onNoteSelected)
        }
    }
}

@Composable
private fun SpaceTaskList(
    tasks: List<Task>,
    onToggleTask: (Task) -> Unit,
    onSelected: (String) -> Unit
) {
    if (tasks.isEmpty()) {
        SpaceEmptyState("No active tasks. Tap + to capture one.")
        return
    }
    LazyColumn(
        modifier = Modifier.heightIn(max = 430.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) {
        items(tasks, key = Task::id) { task ->
            TaskRow(task = task, onToggleTask = onToggleTask, onSelectTask = onSelected)
        }
    }
}

@Composable
private fun SpaceProjectList(projects: List<Project>, onSelected: (String) -> Unit) {
    if (projects.isEmpty()) {
        SpaceEmptyState("No projects yet. Tap + to create one.")
        return
    }
    LazyColumn(
        modifier = Modifier.heightIn(max = 430.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        items(projects, key = Project::id) { project ->
            SpaceTextRow(
                title = project.name,
                subtitle = project.description,
                onClick = { onSelected(project.id) }
            )
        }
    }
}

@Composable
private fun SpaceNoteList(notes: List<Note>, onSelected: (String) -> Unit) {
    if (notes.isEmpty()) {
        SpaceEmptyState("No notes yet. Tap + to write one.")
        return
    }
    LazyColumn(
        modifier = Modifier.heightIn(max = 430.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        items(notes, key = Note::id) { note ->
            SpaceTextRow(
                title = note.title.ifBlank { "Untitled note" },
                subtitle = note.content,
                onClick = { onSelected(note.id) }
            )
        }
    }
}

@Composable
private fun SpaceTextRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SpaceEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
