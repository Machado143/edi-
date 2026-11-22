package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.repository.ClienteRepository
import com.exemplo.oficina.data.repository.NetworkResult
import com.exemplo.oficina.data.repository.VeiculoRepository
import com.exemplo.oficina.domain.model.Cliente
import com.exemplo.oficina.domain.model.Veiculo
import com.exemplo.oficina.ui.util.InputMasks
import com.exemplo.oficina.ui.util.FormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VeiculoUiState(
    val veiculos: List<Veiculo> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormOpen: Boolean = false,
    val selectedVeiculo: Veiculo? = null,
    val veiculoParaExcluir: Veiculo? = null,

    // Form inputs
    val clienteIdInput: Int? = null,
    val placaInput: String = "",
    val modeloInput: String = "",
    val marcaInput: String = "",
    val anoInput: String = ""
)

class VeiculoViewModel(
    private val veiculoRepository: VeiculoRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VeiculoUiState())
    val uiState: StateFlow<VeiculoUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val veiculosResult = veiculoRepository.getVeiculos()
            val clientesResult = clienteRepository.getClientes()
            val erro = (veiculosResult as? NetworkResult.Error)?.message
                ?: (clientesResult as? NetworkResult.Error)?.message

            _uiState.update {
                it.copy(
                    isLoading = false,
                    veiculos = veiculosResult.getOrDefault(emptyList()),
                    clientes = clientesResult.getOrDefault(emptyList()),
                    errorMessage = erro
                )
            }
        }
    }

    fun openForm(veiculo: Veiculo? = null) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                selectedVeiculo = veiculo,
                clienteIdInput = veiculo?.clienteId ?: it.clientes.firstOrNull()?.id,
                placaInput = InputMasks.plate(veiculo?.placa ?: ""),
                modeloInput = veiculo?.modelo ?: "",
                marcaInput = veiculo?.marca ?: "",
                anoInput = veiculo?.ano?.toString() ?: ""
            )
        }
    }

    fun closeForm() {
        _uiState.update { it.copy(isFormOpen = false, selectedVeiculo = null) }
    }

    fun onClienteSelected(clienteId: Int) = _uiState.update { it.copy(clienteIdInput = clienteId) }
    fun onPlacaChange(v: String) = _uiState.update { it.copy(placaInput = InputMasks.plate(v)) }
    fun onModeloChange(v: String) = _uiState.update { it.copy(modeloInput = v) }
    fun onMarcaChange(v: String) = _uiState.update { it.copy(marcaInput = v) }
    fun onAnoChange(v: String) = _uiState.update { it.copy(anoInput = v) }

    fun saveVeiculo() {
        val s = _uiState.value
        if (s.isSaving) return
        val clienteId = s.clienteIdInput
        val ano = s.anoInput.toIntOrNull()

        val validationMessage = FormValidation.validateVeiculo(clienteId, s.placaInput, s.modeloInput, ano)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSaving = true) }
            val veiculo = Veiculo(
                id = s.selectedVeiculo?.id,
                clienteId = clienteId!!,
                placa = s.placaInput.replace("-", "").uppercase(),
                modelo = s.modeloInput,
                marca = s.marcaInput,
                ano = ano!!
            )

            val result: NetworkResult<*> =
                if (veiculo.id == null) veiculoRepository.createVeiculo(veiculo) else veiculoRepository.updateVeiculo(veiculo)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    closeForm()
                    loadData()
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

    fun solicitarExclusao(veiculo: Veiculo) {
        _uiState.update { it.copy(veiculoParaExcluir = veiculo) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(veiculoParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val veiculo = _uiState.value.veiculoParaExcluir ?: return
        val id = veiculo.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, veiculoParaExcluir = null) }
            val result = veiculoRepository.deleteVeiculo(id)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(successMessage = "Veículo excluído com sucesso!") }
                    loadData()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
