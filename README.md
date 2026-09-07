# Vector N

Vector N is a local-first Android productivity launcher inspired by the speed and list-first interaction of minimalist launchers. It combines direct app access with a deliberately small surface for Today tasks, projects, and notes. It does not contain Niagara Launcher code, artwork, branding, or assets.

## Current experience

- Minimal single-page Home with fixed, independently scrollable Today, Projects, Notes, and Favorite apps sections
- Favorite apps remain pinned below the three work sections
- Spring-driven, haptic `★/A-Z/#` edge scrubbing that opens and moves through the installed-app list
- Search across apps, tasks, projects, notes, and project resources
- Literal task controls: `[ ] Task` and `[✓]` with completed titles struck through
- Project-isolated tasks and notes; project tasks enter Today only when the user chooses
- Project creation, editing, deletion, progress, tasks, notes, and openable resource links
- Note auto-save and confirmed deletion for notes, tasks, resources, and projects
- Favorite-app selection, ordering, long-press actions, and optional notification previews
- User-selected wallpaper, wallpaper removal/dimming, themes, accent color, and display tuning
- Home section visibility and ordering in Settings
- Android Home-role setup in Settings
- Android backup plus validated portable JSON export/restore for work data and preferences

Wallpaper files are intentionally not embedded in portable JSON backups because Android document permissions and source URIs are device-specific. The rest of the appearance configuration is preserved.

## Architecture

- Kotlin and Jetpack Compose
- Room database for projects, tasks, notes, and resources
- Preferences DataStore for favorites, Home layout, wallpaper reference, and appearance
- Explicit Room migrations; the current schema is version 4
- No network dependency, polling loop, or always-running background worker for core launcher behavior

The product direction is defined in [`VECTOR_LAUNCHER_PRODUCT_DEFINITION.md`](VECTOR_LAUNCHER_PRODUCT_DEFINITION.md).

## Build and verify

```powershell
$env:ANDROID_HOME = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { "$env:LOCALAPPDATA\Android\Sdk" }
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleRelease --console=plain
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Install on a connected Android device

```powershell
& "$env:ANDROID_HOME\platform-tools\adb.exe" devices
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r -g app\build\outputs\apk\debug\app-debug.apk
```

After installation, open **Settings → Launcher setup → Set as Home**. Notification previews remain optional and require the user to grant notification-listener access in Android settings.
