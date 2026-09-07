package com.vector.launch.core.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

fun openExternalUri(context: Context, uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "No app can open this link.", Toast.LENGTH_SHORT).show()
        }
}
