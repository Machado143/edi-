package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.local.SettingsRepository
import com.exemplo.oficina.data.local.UserPreferencesRepository
import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.repository.apiError
import com.exemplo.oficina.data.repository.connectionOrServerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class SettingsUiState(
    val themeMode: String = "SISTEMA", // SISTEMA, CLARO, ESCURO
    val displayName: String = "Administrador Oficina",
    val notificationsEnabled: Boolean = true,
    val defaultOsSort: String = "DATA", // DATA, CLIENTE, STATUS
    val serverUrl: String = "http://10.0.2.2/oficina_api/",
    val testMessage: String? = null,
    val isTesting: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiServiceProvider: () -> ApiService,
    private val recreateApiService: (String) -> Unit
) : ViewModel() {

    private val testState = MutableStateFlow(Pair<String?, Boolean>(null, false))

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        userPreferencesRepository.baseUrlFlow,
        testState
    ) { settings, serverUrl, test ->
        SettingsUiState(
            themeMode = settings.themeMode,
            displayName = settings.displayName,
            notificationsEnabled = settings.notificationsEnabled,
            defaultOsSort = settings.defaultOsSort,
            serverUrl = serverUrl,
            testMessage = test.first,
            isTesting = test.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.saveThemeMode(mode)
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            settingsRepository.saveDisplayName(name)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveNotificationsEnabled(enabled)
        }
    }

    fun setDefaultOsSort(sort: String) {
        viewModelScope.launch {
            settingsRepository.saveDefaultOsSort(sort)
        }
    }

    fun saveAndTestConnection(rawUrl: String) {
        val trimmed = rawUrl.trim()
        val url = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        val validUrl = (url.startsWith("http://") || url.startsWith("https://")) &&
            url.toHttpUrlOrNull() != null
        if (!validUrl) {
            testState.value = Pair("URL inválida. Exemplo: http://192.168.0.10/oficina_api/", false)
            return
        }
        testState.value = Pair(null, true)
        viewModelScope.launch {
            try {
                recreateApiService(url)
                val response = apiServiceProvider().getResumo()
                testState.value = when {
                    response.isSuccessful -> Pair("Conexão bem-sucedida. URL salva.", false)
                    response.code() == 401 -> Pair("Servidor alcançado. URL salva. Faça login para continuar.", false)
                    else -> Pair(apiError(response) ?: "Não foi possível conectar ao servidor.", false)
                }
            } catch (exception: Exception) {
                testState.value = Pair(connectionOrServerMessage(exception), false)
            }
        }
    }
}
