package com.vector.launch.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. AMBER (Warm Dark Stone)
private val AmberBaseColorScheme = darkColorScheme(
    background = Color(0xFF1C1917),
    surface = Color(0xFF292524),
    surfaceVariant = Color(0xFF44403C),
    onBackground = Color(0xFFFAFAF9),
    onSurface = Color(0xFFFAFAF9),
    onSurfaceVariant = Color(0xFFA8A29E),
    primary = Color(0xFFF59E0B),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    outline = Color(0xFF57534E),
    error = Color(0xFFEF4444)
)

// 2. DARK (Slate Midnight)
private val DarkBaseColorScheme = darkColorScheme(
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    primary = Color(0xFF818CF8),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    outline = Color(0xFF475569),
    error = Color(0xFFEF4444)
)

// 3. LIGHT (Clean Slate Light)
private val LightBaseColorScheme = lightColorScheme(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    primary = Color(0xFF4F46E5),
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444)
)

// 4. OLED BLACK (Pitch Black #000000)
private val OledBaseColorScheme = darkColorScheme(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF141414),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFA1A1AA),
    primary = Color(0xFFA5B4FC),
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFFEEF2FF),
    outline = Color(0xFF222222),
    error = Color(0xFFEF4444)
)

fun parseAccentColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#").trim()
        val colorInt = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFFF59E0B) // Fallback to Amber accent
    }
}

@Composable
fun VectorTheme(
    themeMode: String = "AMBER",
    accentColorHex: String = "#F59E0B",
    content: @Composable () -> Unit
) {
    val baseScheme = when (themeMode.uppercase()) {
        "DARK" -> DarkBaseColorScheme
        "LIGHT" -> LightBaseColorScheme
        "OLED", "OLED_BLACK" -> OledBaseColorScheme
        else -> AmberBaseColorScheme
    }

    val accentColor = parseAccentColor(accentColorHex)
    val colorScheme = baseScheme.copy(
        primary = accentColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VectorTypography,
        content = content
    )
}

