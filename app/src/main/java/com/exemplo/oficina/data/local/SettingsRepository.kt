package com.exemplo.oficina.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "oficina_settings")

data class AppSettings(
    val themeMode: String = "SISTEMA",
    val displayName: String = "Administrador Oficina",
    val notificationsEnabled: Boolean = true,
    val defaultOsSort: String = "DATA"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // SISTEMA, CLARO, ESCURO
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DEFAULT_OS_SORT = stringPreferencesKey("default_os_sort") // DATA, CLIENTE, STATUS
    }

    val themeModeFlow: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "SISTEMA"
    }

    val displayNameFlow: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.DISPLAY_NAME] ?: "Administrador Oficina"
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val defaultOsSortFlow: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_OS_SORT] ?: "DATA"
    }

    val settingsFlow: Flow<AppSettings> = combine(
        themeModeFlow,
        displayNameFlow,
        notificationsEnabledFlow,
        defaultOsSortFlow
    ) { themeMode, displayName, notificationsEnabled, defaultOsSort ->
        AppSettings(themeMode, displayName, notificationsEnabled, defaultOsSort)
    }

    suspend fun saveThemeMode(mode: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun saveDisplayName(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = name
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveDefaultOsSort(sort: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_OS_SORT] = sort
        }
    }
}
