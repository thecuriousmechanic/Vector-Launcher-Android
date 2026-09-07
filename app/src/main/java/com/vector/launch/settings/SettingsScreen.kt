package com.vector.launch.settings

import androidx.activity.compose.BackHandler
import android.app.role.RoleManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vector.launch.core.theme.parseAccentColor
import com.vector.launch.core.model.DashboardWidgetConfig
import com.vector.launch.core.model.WidgetType
import com.vector.launch.core.ui.DisplayControlUtils
import com.vector.launch.core.ui.VectorCard
import com.vector.launch.core.ui.WallpaperUtils
import com.vector.launch.notifications.hasNotificationPreviewAccess
import com.vector.launch.notifications.openNotificationPreviewSettings
import kotlin.math.roundToInt

data class AccentColorOption(val name: String, val hex: String)

val CURATED_ACCENT_COLORS = listOf(
    AccentColorOption("Amber", "#F59E0B"),
    AccentColorOption("White", "#FFFFFF"),
    AccentColorOption("Blue", "#3B82F6"),
    AccentColorOption("Green", "#10B981"),
    AccentColorOption("Red", "#EF4444"),
    AccentColorOption("Purple", "#8B5CF6"),
    AccentColorOption("Pink", "#EC4899"),
    AccentColorOption("Cyan", "#06B6D4")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    if (onBackClick != null) {
        BackHandler(onBack = onBackClick)
    }

    var brightness by remember { mutableFloatStateOf(uiState.brightnessValue) }
    var contrast by remember { mutableFloatStateOf(uiState.contrastValue) }
    var saturation by remember { mutableFloatStateOf(uiState.saturationValue) }
    var wallpaperDim by remember { mutableFloatStateOf(uiState.wallpaperDimValue) }
    var advancedAppearanceExpanded by rememberSaveable { mutableStateOf(false) }
    val roleManager = remember(context) { context.getSystemService(RoleManager::class.java) }
    var isDefaultHome by remember {
        mutableStateOf(roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && roleManager.isRoleHeld(RoleManager.ROLE_HOME))
    }
    var hasNotificationAccess by remember { mutableStateOf(hasNotificationPreviewAccess(context)) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uiState.brightnessValue) { brightness = uiState.brightnessValue }
    LaunchedEffect(uiState.contrastValue) { contrast = uiState.contrastValue }
    LaunchedEffect(uiState.saturationValue) { saturation = uiState.saturationValue }
    LaunchedEffect(uiState.wallpaperDimValue) { wallpaperDim = uiState.wallpaperDimValue }

    DisposableEffect(lifecycleOwner, context, roleManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultHome = roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                    roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                hasNotificationAccess = hasNotificationPreviewAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val homeRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultHome = roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.setWallpaperUri(uri.toString())
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(context.contentResolver, it) }
    }
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingRestoreUri = uri
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Restore this backup?") },
            text = { Text("Current tasks, projects, notes, resources, favorites, and appearance settings will be replaced.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRestoreUri = null
                        viewModel.restoreBackup(context.contentResolver, uri)
                    },
                    enabled = !uiState.isBackupBusy
                ) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) { Text("Cancel") }
            }
        )
    }

    val displayColorFilter = remember(brightness, contrast, saturation) {
        DisplayControlUtils.buildDisplayColorFilter(
            brightness = brightness,
            contrast = contrast,
            saturation = saturation
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    colorFilter = displayColorFilter
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Launcher setup",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Finish Android integration once, then Vector stays out of the way.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                VectorCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SetupAction(
                            title = "Default Home app",
                            description = if (isDefaultHome) {
                                "Vector N opens when you press Home."
                            } else {
                                "Use Vector N as the device starting screen."
                            },
                            actionLabel = if (isDefaultHome) "Active" else "Set as Home",
                            actionEnabled = !isDefaultHome,
                            onAction = {
                                val intent = if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                } else {
                                    Intent(Settings.ACTION_HOME_SETTINGS)
                                }
                                homeRoleLauncher.launch(intent)
                            }
                        )
                        HorizontalDivider()
                        SetupAction(
                            title = "Notification previews",
                            description = if (hasNotificationAccess) {
                                "Enabled for favorite apps."
                            } else {
                                "Optional: show useful notification text beside favorites."
                            },
                            actionLabel = if (hasNotificationAccess) "Enabled" else "Enable",
                            actionEnabled = !hasNotificationAccess,
                            onAction = { openNotificationPreviewSettings(context) }
                        )
                    }
                }
            }

            item {
                VectorCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Data safety",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Android backup is enabled. You can also keep a portable local copy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { exportBackupLauncher.launch("vector-n-backup.json") },
                                enabled = !uiState.isBackupBusy,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Export") }
                            OutlinedButton(
                                onClick = { restoreBackupLauncher.launch(arrayOf("application/json", "text/plain")) },
                                enabled = !uiState.isBackupBusy,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Restore") }
                        }
                        if (uiState.isBackupBusy) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        uiState.message?.let { message ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = viewModel::clearMessage) { Text("Dismiss") }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Show only what helps you start, in the order you need it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                VectorCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val orderedSections = uiState.homeSections
                            .sortedWith(
                                compareBy<DashboardWidgetConfig> {
                                    when (it.type) {
                                        WidgetType.CLOCK -> 0
                                        WidgetType.APP_LAUNCHER -> 2
                                        else -> 1
                                    }
                                }.thenBy(DashboardWidgetConfig::position)
                            )
                        val movableSections = orderedSections.filter {
                            it.type != WidgetType.CLOCK && it.type != WidgetType.APP_LAUNCHER
                        }
                        orderedSections.forEach { section ->
                            val movableIndex = movableSections.indexOfFirst { it.id == section.id }
                            HomeSectionSetting(
                                section = section,
                                canMoveUp = movableIndex > 0,
                                canMoveDown = movableIndex >= 0 && movableIndex < movableSections.lastIndex,
                                onVisibilityChange = { viewModel.toggleHomeSection(section.id) },
                                onMoveUp = { viewModel.moveHomeSection(section.id, -1) },
                                onMoveDown = { viewModel.moveHomeSection(section.id, 1) }
                            )
                        }
                        TextButton(
                            onClick = viewModel::resetHomeSections,
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Restore default layout") }
                    }
                }
            }

            item {
                Text(
                    text = "Wallpaper",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Choose the image behind your favorites and tune its readability.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                WallpaperSettingsPanel(
                    wallpaperUri = uiState.wallpaperUri,
                    wallpaperDim = wallpaperDim,
                    onChoose = { wallpaperPickerLauncher.launch(arrayOf("image/*")) },
                    onRemove = viewModel::removeWallpaper,
                    onDimChange = { wallpaperDim = it },
                    onDimChangeFinished = { viewModel.setWallpaperDimValue(wallpaperDim) }
                )
            }

            item {
                // Theme Selector Card (Amber, Dark, Light, OLED Black)
                VectorCard {
                    Column {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val themes = listOf(
                            "AMBER" to "Amber",
                            "DARK" to "Dark",
                            "LIGHT" to "Light",
                            "OLED" to "OLED Black"
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            themes.forEach { (modeKey, modeName) ->
                                FilterChip(
                                    selected = uiState.themeMode.equals(modeKey, ignoreCase = true),
                                    onClick = { viewModel.setThemeMode(modeKey) },
                                    label = { Text(modeName, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Accent / Font Color Picker Card
                VectorCard {
                    Column {
                        Text(
                            text = "Accent color",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CURATED_ACCENT_COLORS.forEach { option ->
                                val isSelected = uiState.accentColorHex.equals(option.hex, ignoreCase = true)
                                val optionColor = parseAccentColor(option.hex)

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setAccentColorHex(option.hex) },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(optionColor)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (option.hex.equals("#FFFFFF", ignoreCase = true)) Color.Gray else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                            )
                                            Text(option.name, fontSize = 12.sp)
                                        }
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Accent color ${option.name}"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                VectorCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = { advancedAppearanceExpanded = !advancedAppearanceExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Advanced appearance",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(if (advancedAppearanceExpanded) "Hide" else "Show")
                        }

                        if (advancedAppearanceExpanded) {
                            DisplayTuningSlider(
                                label = "Brightness",
                                value = brightness,
                                onValueChange = { brightness = it },
                                onValueChangeFinished = { viewModel.setBrightnessValue(brightness) },
                                valueRange = 0.0f..1.0f
                            )
                            DisplayTuningSlider(
                                label = "Contrast",
                                value = contrast,
                                onValueChange = { contrast = it },
                                onValueChangeFinished = { viewModel.setContrastValue(contrast) },
                                valueRange = 0.5f..1.5f
                            )
                            DisplayTuningSlider(
                                label = "Saturation",
                                value = saturation,
                                onValueChange = { saturation = it },
                                onValueChangeFinished = { viewModel.setSaturationValue(saturation) },
                                valueRange = 0.0f..2.0f
                            )
                        }

                    }
                }
            }

            item {
                VectorCard {
                    Column {
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Reset the short list shown on your launcher.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = viewModel::clearFavorites,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Clear favorite apps")
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vector N • Core features work offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionSetting(
    section: DashboardWidgetConfig,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onVisibilityChange: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val label = when (section.type) {
        WidgetType.CLOCK -> "Clock and date"
        WidgetType.TASKS -> "Today"
        WidgetType.PROJECTS -> "Projects"
        WidgetType.NOTES -> "Notes"
        WidgetType.APP_LAUNCHER -> "Favorite apps (bottom)"
        else -> section.title
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (section.type != WidgetType.CLOCK && section.type != WidgetType.APP_LAUNCHER) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move $label up")
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move $label down")
            }
        }
        Switch(
            checked = section.isVisible,
            onCheckedChange = { onVisibilityChange() }
        )
    }
}

@Composable
private fun SetupAction(
    title: String,
    description: String,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedButton(
            onClick = onAction,
            enabled = actionEnabled,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun DisplayTuningSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${(value * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.semantics {
                contentDescription = "$label, ${(value * 100).roundToInt()} percent"
            }
        )
    }
}

@Composable
private fun WallpaperSettingsPanel(
    wallpaperUri: String?,
    wallpaperDim: Float,
    onChoose: () -> Unit,
    onRemove: () -> Unit,
    onDimChange: (Float) -> Unit,
    onDimChangeFinished: () -> Unit
) {
    val context = LocalContext.current
    val wallpaperBitmap by produceState<Bitmap?>(initialValue = null, key1 = wallpaperUri) {
        value = wallpaperUri?.takeIf(String::isNotBlank)?.let { uri ->
            WallpaperUtils.loadDownsampledWallpaper(context, uri, reqWidth = 900, reqHeight = 500)
        }
    }

    VectorCard(cornerRadius = 24.dp, padding = 0.dp) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val preview = wallpaperBitmap
                if (preview != null) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = "Current wallpaper preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = wallpaperDim.coerceIn(0f, 1f)))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No wallpaper selected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vector N is using the theme background",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onChoose,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (wallpaperUri.isNullOrBlank()) "Choose image" else "Replace image")
                    }
                    if (!wallpaperUri.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = onRemove,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Remove")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dim wallpaper", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${(wallpaperDim * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = wallpaperDim,
                    onValueChange = onDimChange,
                    onValueChangeFinished = onDimChangeFinished,
                    valueRange = 0f..1f,
                    modifier = Modifier.semantics {
                        contentDescription = "Wallpaper dim, ${(wallpaperDim * 100).roundToInt()} percent"
                    }
                )
            }
        }
    }
}
