package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.repository.NetworkResult
import com.exemplo.oficina.data.repository.ServicoRepository
import com.exemplo.oficina.domain.model.Servico
import com.exemplo.oficina.ui.util.FormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServicoUiState(
    val servicos: List<Servico> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormOpen: Boolean = false,
    val selectedServico: Servico? = null,
    val servicoParaExcluir: Servico? = null,

    // Form inputs
    val nomeInput: String = "",
    val descricaoInput: String = "",
    val precoPadraoInput: String = ""
)

class ServicoViewModel(
    private val repository: ServicoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicoUiState())
    val uiState: StateFlow<ServicoUiState> = _uiState.asStateFlow()

    init {
        loadServicos()
    }

    fun loadServicos() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.getServicos()) {
                is NetworkResult.Success ->
                    _uiState.update { it.copy(servicos = result.data, isLoading = false) }
                is NetworkResult.Error ->
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun openForm(servico: Servico? = null) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                selectedServico = servico,
                nomeInput = servico?.nome ?: "",
                descricaoInput = servico?.descricao ?: "",
                precoPadraoInput = servico?.precoPadrao?.toString() ?: ""
            )
        }
    }

    fun closeForm() {
        _uiState.update { it.copy(isFormOpen = false, selectedServico = null) }
    }

    fun onNomeChange(v: String) = _uiState.update { it.copy(nomeInput = v) }
    fun onDescricaoChange(v: String) = _uiState.update { it.copy(descricaoInput = v) }
    fun onPrecoPadraoChange(v: String) = _uiState.update { it.copy(precoPadraoInput = v) }

    fun saveServico() {
        val s = _uiState.value
        if (s.isSaving) return
        val preco = s.precoPadraoInput.toDoubleOrNull()

        val validationMessage = FormValidation.validateServico(s.nomeInput, preco)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSaving = true) }
            val servico = Servico(
                id = s.selectedServico?.id,
                nome = s.nomeInput,
                descricao = s.descricaoInput,
                precoPadrao = preco!!
            )

            val result: NetworkResult<*> =
                if (servico.id == null) repository.createServico(servico) else repository.updateServico(servico)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    closeForm()
                    loadServicos()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isSaving = false, errorMessage = result.message) }
                }
                else -> _uiState.update { it.copy(isLoading = false, isSaving = false) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun solicitarExclusao(servico: Servico) {
        _uiState.update { it.copy(servicoParaExcluir = servico) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(servicoParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val servico = _uiState.value.servicoParaExcluir ?: return
        val id = servico.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, servicoParaExcluir = null) }
            val result = repository.deleteServico(id)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(successMessage = "Serviço excluído com sucesso!") }
                    loadServicos()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
