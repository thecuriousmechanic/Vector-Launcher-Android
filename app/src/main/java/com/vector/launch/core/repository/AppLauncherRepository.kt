package com.vector.launch.core.repository

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import com.vector.launch.core.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppLauncherRepository(context: Context) {

    private val context = context.applicationContext
    private val cacheMutex = Mutex()

    private var cachedApps: List<AppInfo>? = null

    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            if (!forceRefresh) {
                cachedApps?.let { return@withLock it }
            }

            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val ownPackageName = context.packageName
            val apps = pm.queryIntentActivities(mainIntent, 0)
                .asSequence()
                .filter { it.activityInfo.packageName != ownPackageName }
                .distinctBy { it.activityInfo.packageName }
                .map { resolveInfo ->
                    AppInfo(
                        packageName = resolveInfo.activityInfo.packageName,
                        label = resolveInfo.loadLabel(pm).toString(),
                        icon = resolveInfo.loadIcon(pm)
                    )
                }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, AppInfo::label))
                .toList()

            cachedApps = apps
            apps
        }
    }

    suspend fun getAlphabeticallyGroupedApps(): Map<Char, List<AppInfo>> = withContext(Dispatchers.IO) {
        val apps = getInstalledApps()
        apps.groupBy { it.firstLetter }.toSortedMap()
    }

    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val cleanQuery = query.trim()
        val allApps = getInstalledApps()
        allApps.filter { it.label.contains(cleanQuery, ignoreCase = true) }
    }

    suspend fun getPreferredApps(packages: List<String>): List<AppInfo> = withContext(Dispatchers.IO) {
        if (packages.isEmpty()) return@withContext emptyList()
        val allAppsMap = getInstalledApps().associateBy { it.packageName }
        packages.mapNotNull { pkg -> allAppsMap[pkg] }
    }

    fun launchApp(packageName: String): Boolean {
        return runCatching {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    fun launchAppInfo(packageName: String): Boolean {
        return runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
