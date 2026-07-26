package com.fqrproject.fqr.ui.screentime

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.screenTimeDataStore by preferencesDataStore(name = "screen_time_prefs")

private val TARGET_KEY = intPreferencesKey("target_minutes")

class ScreenTimeRepository(private val context: Context) {

    val targetMinutes: Flow<Int> = context.screenTimeDataStore.data
        .map { prefs -> prefs[TARGET_KEY] ?: 120 }

    suspend fun setTargetMinutes(minutes: Int) {
        context.screenTimeDataStore.edit { prefs ->
            prefs[TARGET_KEY] = minutes
        }
    }
}