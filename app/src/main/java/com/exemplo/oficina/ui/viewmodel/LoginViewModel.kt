package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.local.UserPreferencesRepository
import com.exemplo.oficina.data.repository.AuthRepository
import com.exemplo.oficina.data.repository.NetworkResult
import com.exemplo.oficina.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val usuario: Usuario) : LoginState()
    data class Error(val message: String) : LoginState()
}

data class LoginUiState(
    val emailInput: String = "",
    val senhaInput: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    val loginState: LoginState = LoginState.Idle
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email, loginState = LoginState.Idle) }
    }

    fun onSenhaChange(senha: String) {
        _uiState.update { it.copy(senhaInput = senha, loginState = LoginState.Idle) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.update { it.copy(rememberMe = checked) }
    }

    fun setSessionExpiredMessage(message: String) {
        _uiState.update { it.copy(loginState = LoginState.Error(message)) }
    }

    fun resetState() {
        _uiState.update { it.copy(loginState = LoginState.Idle) }
    }

    fun login() {
        val s = _uiState.value

        // Validação de campos vazios
        if (s.emailInput.trim().isEmpty() || s.senhaInput.trim().isEmpty()) {
            _uiState.update { it.copy(loginState = LoginState.Error("Preencha todos os campos (e-mail e senha).")) }
            return
        }

        _uiState.update { it.copy(loginState = LoginState.Loading) }

        viewModelScope.launch {
            val result = authRepository.login(s.emailInput.trim(), s.senhaInput.trim(), s.rememberMe)
            when (result) {
                is NetworkResult.Success -> {
                    val user = result.data
                    // A sessão e o token real já foram persistidos pelo AuthRepository.
                    _uiState.update { it.copy(loginState = LoginState.Success(user)) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(loginState = LoginState.Error(result.message)) }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(loginState = LoginState.Loading) }
                }
            }
        }
    }
}
