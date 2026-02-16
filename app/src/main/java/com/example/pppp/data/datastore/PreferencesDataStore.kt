package com.example.pppp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferencesDataStore by preferencesDataStore(name = "user_prefs")

object PreferencesKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
}

class PreferencesDataStore(private val context: Context) {
    fun notificationsEnabled(): Flow<Boolean> =
        context.preferencesDataStore.data.map { it[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    fun darkModeEnabled(): Flow<Boolean> =
        context.preferencesDataStore.data.map { it[PreferencesKeys.DARK_MODE_ENABLED] ?: false }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.preferencesDataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.preferencesDataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }
}

