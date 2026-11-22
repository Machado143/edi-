package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.repository.ClienteRepository
import com.exemplo.oficina.data.repository.NetworkResult
import com.exemplo.oficina.domain.model.Cliente
import com.exemplo.oficina.ui.util.FormValidation
import com.exemplo.oficina.ui.util.InputMasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClienteUiState(
    val clientes: List<Cliente> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormOpen: Boolean = false,
    val selectedCliente: Cliente? = null,
    val clienteParaExcluir: Cliente? = null,

    // Campos do formulário
    val nomeInput: String = "",
    val cpfCnpjInput: String = "",
    val telefoneInput: String = "",
    val emailInput: String = "",
    val enderecoInput: String = ""
)

class ClienteViewModel(
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    init {
        loadClientes()
    }

    fun loadClientes() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = clienteRepository.getClientes()) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(clientes = result.data, isLoading = false) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openForm(cliente: Cliente? = null) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                selectedCliente = cliente,
                nomeInput = cliente?.nome ?: "",
                cpfCnpjInput = cliente?.cpfCnpj ?: "",
                telefoneInput = InputMasks.phone(cliente?.telefone ?: ""),
                emailInput = cliente?.email ?: "",
                enderecoInput = cliente?.endereco ?: "",
                errorMessage = null
            )
        }
    }

    fun closeForm() {
        _uiState.update { it.copy(isFormOpen = false, selectedCliente = null, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun onNomeChange(v: String) = _uiState.update { it.copy(nomeInput = v) }
    fun onCpfCnpjChange(v: String) = _uiState.update { it.copy(cpfCnpjInput = v) }
    fun onTelefoneChange(v: String) = _uiState.update { it.copy(telefoneInput = InputMasks.phone(v)) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(emailInput = v) }
    fun onEnderecoChange(v: String) = _uiState.update { it.copy(enderecoInput = v) }

    fun saveCliente() {
        val s = _uiState.value
        if (s.isSaving) return

        val cpfDigits = s.cpfCnpjInput.filter(Char::isDigit)
        val validationMessage = FormValidation.validateCliente(s.nomeInput, cpfDigits)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSaving = true) }
            val clienteToSave = Cliente(
                id = s.selectedCliente?.id,
                nome = s.nomeInput.trim(),
                cpfCnpj = cpfDigits,
                telefone = s.telefoneInput.filter(Char::isDigit),
                email = s.emailInput.trim(),
                endereco = s.enderecoInput.trim()
            )

            val result = if (clienteToSave.id == null) {
                clienteRepository.createCliente(clienteToSave)
            } else {
                clienteRepository.updateCliente(clienteToSave)
            }

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Cliente salvo com sucesso!") }
                    closeForm()
                    loadClientes() // Recarga da lista após inserção/edição
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isSaving = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = false, isSaving = false) }
            }
        }
    }

    fun solicitarExclusao(cliente: Cliente) {
        _uiState.update { it.copy(clienteParaExcluir = cliente) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(clienteParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val cliente = _uiState.value.clienteParaExcluir ?: return
        val id = cliente.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, clienteParaExcluir = null) }
            val result = clienteRepository.deleteCliente(id)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(successMessage = "Cliente excluído com sucesso!") }
                    loadClientes() // Recarga da lista após exclusão
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
