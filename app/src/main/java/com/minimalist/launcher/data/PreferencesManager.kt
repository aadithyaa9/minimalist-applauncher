package com.minimalist.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val PINNED_APPS = stringSetPreferencesKey("pinned_apps")
        val BLOCKED_APPS = stringSetPreferencesKey("blocked_apps")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val SCRATCH_PAD = stringPreferencesKey("scratch_pad")
        val HABITS_JSON = stringPreferencesKey("habits_json")
    }

    val pinnedApps: Flow<Set<String>> = context.dataStore.data.map { it[Keys.PINNED_APPS] ?: emptySet() }
    val blockedApps: Flow<Set<String>> = context.dataStore.data.map { it[Keys.BLOCKED_APPS] ?: emptySet() }
    val hiddenApps: Flow<Set<String>> = context.dataStore.data.map { it[Keys.HIDDEN_APPS] ?: emptySet() }
    val scratchPad: Flow<String> = context.dataStore.data.map { it[Keys.SCRATCH_PAD] ?: "" }
    val habitsJson: Flow<String?> = context.dataStore.data.map { it[Keys.HABITS_JSON] }

    suspend fun togglePin(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PINNED_APPS] ?: emptySet()
            prefs[Keys.PINNED_APPS] = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun toggleBlock(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_APPS] ?: emptySet()
            prefs[Keys.BLOCKED_APPS] = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun toggleHidden(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_APPS] ?: emptySet()
            prefs[Keys.HIDDEN_APPS] = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun saveScratchPad(text: String) {
        context.dataStore.edit { it[Keys.SCRATCH_PAD] = text }
    }

    suspend fun saveHabitsJson(json: String) {
        context.dataStore.edit { it[Keys.HABITS_JSON] = json }
    }
}
