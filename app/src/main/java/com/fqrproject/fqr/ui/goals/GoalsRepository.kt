package com.fqrproject.fqr.ui.goals

import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.encodeToStream

private val Context.dataStore by preferencesDataStore(name = "goals_prefs")
private val GOALS_KEY = stringPreferencesKey("goals_json")


class GoalsRepository(private val context: Context) {
    val goals: Flow<List<Goal>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[GOALS_KEY] ?: "[]"
            Json.decodeFromString<List<Goal>>(json)
        }

    suspend fun saveGoals(goals: List<Goal>) {
        val json = Json.encodeToString(goals)
        context.dataStore.edit { prefs ->
            prefs[GOALS_KEY] = json
        }
    }
}

