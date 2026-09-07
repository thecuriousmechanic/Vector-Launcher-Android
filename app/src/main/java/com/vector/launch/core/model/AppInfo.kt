package com.vector.launch.core.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null
) {
    val firstLetter: Char
        get() {
            val char = label.trim().firstOrNull()?.uppercaseChar() ?: '#'
            return if (char in 'A'..'Z') char else '#'
        }
}
