package com.exemplo.oficina.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "oficina_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val BASE_URL = stringPreferencesKey("base_url")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLE = stringPreferencesKey("user_role")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    val baseUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BASE_URL] ?: "http://10.0.2.2/oficina_api/"
    }

    val userTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_TOKEN]
    }

    val loggedUserIdFlow: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    val loggedUserNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: "Usuário"
    }

    val loggedUserRoleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ROLE] ?: "MECANICO"
    }

    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMEMBER_ME] ?: true
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_URL] = if (url.endsWith("/")) url else "$url/"
        }
    }

    suspend fun saveUserSession(
        id: Int,
        name: String,
        role: String,
        token: String = "",
        rememberMe: Boolean = true
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_ROLE] = role
            preferences[PreferencesKeys.USER_TOKEN] = token
            preferences[PreferencesKeys.REMEMBER_ME] = rememberMe
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences.remove(PreferencesKeys.USER_ROLE)
            preferences.remove(PreferencesKeys.REMEMBER_ME)
        }
    }
}
