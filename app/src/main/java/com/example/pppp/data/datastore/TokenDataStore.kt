package com.example.pppp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object TokenKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USERNAME = stringPreferencesKey("username")
    val ROLES = stringPreferencesKey("roles")
    val USER_ID = stringPreferencesKey("user_id")
}

class TokenDataStore(private val context: Context) {
    suspend fun saveTokens(accessToken: String, refreshToken: String, username: String, roles: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[TokenKeys.ACCESS_TOKEN] = accessToken
            prefs[TokenKeys.REFRESH_TOKEN] = refreshToken
            prefs[TokenKeys.USERNAME] = username
            prefs[TokenKeys.ROLES] = roles
            prefs[TokenKeys.USER_ID] = userId
        }
    }

    fun getAccessToken(): Flow<String?> = context.dataStore.data.map { it[TokenKeys.ACCESS_TOKEN] }
    fun getRefreshToken(): Flow<String?> = context.dataStore.data.map { it[TokenKeys.REFRESH_TOKEN] }
    fun getUsername(): Flow<String?> = context.dataStore.data.map { it[TokenKeys.USERNAME] }
    fun getRoles(): Flow<String?> = context.dataStore.data.map { it[TokenKeys.ROLES] }
    fun getUserId(): Flow<String?> = context.dataStore.data.map { it[TokenKeys.USER_ID] } // Método para obtener userId

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
