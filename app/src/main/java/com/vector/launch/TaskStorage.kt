package com.vector.launch

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "vector_tasks"
)

object TaskStorage {

    suspend fun saveTaskState(
        context: Context,
        taskId: String,
        completed: Boolean
    ) {

        val key =
            booleanPreferencesKey(taskId)

        context.dataStore.edit {

            it[key] = completed
        }
    }

    suspend fun loadTaskState(
        context: Context,
        taskId: String
    ): Boolean {

        val key =
            booleanPreferencesKey(taskId)

        val prefs =
            context.dataStore.data.first()

        return prefs[key] ?: false
    }
}