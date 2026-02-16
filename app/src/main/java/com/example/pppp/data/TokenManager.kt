package com.example.pppp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_preferences")

class TokenManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val IS_ADMIN_KEY = booleanPreferencesKey("is_admin")
    }

    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: Long,
        username: String,
        email: String,
        isAdmin: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId.toString()
            preferences[USERNAME_KEY] = username
            preferences[EMAIL_KEY] = email
            preferences[IS_ADMIN_KEY] = isAdmin
        }
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val isAdmin: Flow<Boolean> = context.dataStore.data.map { it[IS_ADMIN_KEY] ?: false }

    suspend fun clearAuthData() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun updateAccessToken(newToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = newToken
        }
    }
}
