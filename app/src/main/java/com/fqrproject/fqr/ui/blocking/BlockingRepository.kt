package com.fqrproject.fqr.ui.blocking

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.blockingDataStore by preferencesDataStore(name = "apps_prefs")

private val BLOCKED_APPS_KEY = stringPreferencesKey("blocked_apps_json")
private val BLOCKING_ENABLED_KEY = booleanPreferencesKey("blocking_enabled")

class BlockingRepository(private val context: Context) {

    val apps: Flow<List<String>> = context.blockingDataStore.data
        .map { prefs ->
            val json = prefs[BLOCKED_APPS_KEY] ?: "[]"
            Json.decodeFromString<List<String>>(json)
        }

    val blockingEnabled: Flow<Boolean> = context.blockingDataStore.data
        .map { prefs -> prefs[BLOCKING_ENABLED_KEY] ?: false }

    suspend fun saveBlockedApps(apps: List<String>) {
        val json = Json.encodeToString(apps)
        context.blockingDataStore.edit { prefs ->
            prefs[BLOCKED_APPS_KEY] = json
        }
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.blockingDataStore.edit { prefs ->
            prefs[BLOCKING_ENABLED_KEY] = enabled
        }
    }
}