package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.local.UserPreferencesRepository
import com.exemplo.oficina.data.repository.DashboardRepository
import com.exemplo.oficina.data.repository.NetworkResult
import com.exemplo.oficina.domain.model.ResumoDashboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val usuarioNome: String = "Usuário",
    val usuarioCargo: String = "MECANICO",
    val resumo: ResumoDashboard = ResumoDashboard(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
        loadDashboard()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userPreferencesRepository.loggedUserNameFlow.collectLatest { name ->
                _uiState.update { it.copy(usuarioNome = name) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.loggedUserRoleFlow.collectLatest { role ->
                _uiState.update { it.copy(usuarioCargo = role) }
            }
        }
    }

    fun loadDashboard() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = dashboardRepository.getResumo()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resumo = result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
