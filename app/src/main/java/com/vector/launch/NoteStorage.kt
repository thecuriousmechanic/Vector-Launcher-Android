package com.vector.launch

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.noteDataStore by preferencesDataStore(
    name = "vector_notes"
)

object NoteStorage {

    suspend fun saveNote(
        context: Context,
        noteId: String,
        content: String
    ) {

        val key =
            stringPreferencesKey(noteId)

        context.noteDataStore.edit {

            it[key] = content
        }
    }

    suspend fun loadNote(
        context: Context,
        noteId: String
    ): String {

        val key =
            stringPreferencesKey(noteId)

        val prefs =
            context.noteDataStore.data.first()

        return prefs[key] ?: ""
    }
}