package com.exemplo.oficina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.oficina.data.local.SettingsRepository
import com.exemplo.oficina.data.repository.ClienteRepository
import com.exemplo.oficina.data.repository.NetworkResult
import com.exemplo.oficina.data.repository.OrdemServicoRepository
import com.exemplo.oficina.data.repository.ServicoRepository
import com.exemplo.oficina.data.repository.VeiculoRepository
import com.exemplo.oficina.domain.model.Cliente
import com.exemplo.oficina.domain.model.OSServicoItem
import com.exemplo.oficina.domain.model.OrdemServico
import com.exemplo.oficina.domain.model.OsCalculations
import com.exemplo.oficina.ui.util.FormValidation
import com.exemplo.oficina.domain.model.Servico
import com.exemplo.oficina.domain.model.StatusOS
import com.exemplo.oficina.domain.model.Veiculo
import com.exemplo.oficina.ui.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrdemServicoListUiState(
    val ordens: List<OrdemServico> = emptyList(),
    val filteredOrdens: List<OrdemServico> = emptyList(),
    val statusFilter: String? = null,
    val searchInput: String = "",
    val appliedSearchQuery: String = "",
    val defaultSortMode: String = "DATA",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val osParaExcluir: OrdemServico? = null
)

data class OrdemServicoFormUiState(
    val id: Int? = null,
    val veiculos: List<Veiculo> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val todosServicos: List<Servico> = emptyList(),
    val selectedVeiculoId: Int? = null,
    val status: String = StatusOS.ABERTA,
    val observacoesInput: String = "",
    val itens: List<OSServicoItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,

    val isAddItemDialogOpen: Boolean = false,
    val selectedServicoId: Int? = null,
    val quantidadeInput: String = "1",
    val valorCobradoInput: String = "0.0"
) {
    val valorTotalCalculado: Double
        get() = OsCalculations.calcularTotal(itens)
}

class OrdemServicoViewModel(
    private val ordemServicoRepository: OrdemServicoRepository,
    private val veiculoRepository: VeiculoRepository,
    private val clienteRepository: ClienteRepository,
    private val servicoRepository: ServicoRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val notificationHelper: NotificationHelper? = null
) : ViewModel() {

    private val _listState = MutableStateFlow(OrdemServicoListUiState())
    val listState: StateFlow<OrdemServicoListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(OrdemServicoFormUiState())
    val formState: StateFlow<OrdemServicoFormUiState> = _formState.asStateFlow()

    private var searchJob: Job? = null
    private var originalStatus: String? = null

    init {
        observeSettings()
        loadOrdens()
    }

    private fun observeSettings() {
        settingsRepository?.let { repo ->
            viewModelScope.launch {
                repo.defaultOsSortFlow.collectLatest { sort ->
                    _listState.update { it.copy(defaultSortMode = sort) }
                    sortOrdensList()
                }
            }
        }
    }

    fun loadOrdens() {
        _listState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = ordemServicoRepository.getOrdens()) {
                is NetworkResult.Success -> {
                    _listState.update { it.copy(ordens = result.data, isLoading = false) }
                    sortOrdensList()
                }
                is NetworkResult.Error -> {
                    _listState.update { it.copy(ordens = emptyList(), filteredOrdens = emptyList(), errorMessage = result.message, isLoading = false) }
                }
                is NetworkResult.Loading -> {
                    _listState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun alterarStatus(id: Int, status: String) {
        viewModelScope.launch {
            when (val result = ordemServicoRepository.updateStatus(id, status)) {
                is NetworkResult.Success -> {
                    loadOrdens()
                    notificationHelper?.notificarMudancaStatus(id, status)
                }
                is NetworkResult.Error -> _listState.update { it.copy(errorMessage = result.message) }
                is NetworkResult.Loading -> {}
            }
        }
    }
    private fun sortOrdensList() {
        val currentOrdens = _listState.value.ordens
        val sorted = when (_listState.value.defaultSortMode) {
            "CLIENTE" -> currentOrdens.sortedWith(
                compareBy<OrdemServico> { it.nomeCliente?.trim()?.lowercase(Locale("pt", "BR")) ?: "" }
                    .thenByDescending { it.dataAbertura }
            )
            "STATUS" -> currentOrdens.sortedWith(
                compareBy<OrdemServico> { statusOrder(it.status) }
                    .thenByDescending { it.dataAbertura }
            )
            else -> currentOrdens.sortedByDescending { it.dataAbertura } // DATA
        }
        _listState.update { it.copy(ordens = sorted) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _listState.value
        val query = state.appliedSearchQuery.trim().lowercase(Locale("pt", "BR"))
        val filtered = state.ordens.filter { os ->
            val matchesStatus = state.statusFilter == null || os.status == state.statusFilter
            val matchesSearch = query.isBlank() ||
                os.placa?.lowercase(Locale("pt", "BR"))?.contains(query) == true ||
                os.nomeCliente?.lowercase(Locale("pt", "BR"))?.contains(query) == true
            matchesStatus && matchesSearch
        }
        _listState.update { it.copy(filteredOrdens = filtered) }
    }

    private fun statusOrder(status: String): Int = when (status) {
        StatusOS.ABERTA -> 0
        StatusOS.EM_ANDAMENTO -> 1
        StatusOS.FINALIZADA -> 2
        StatusOS.CANCELADA -> 3
        else -> 4
    }

    fun filterByStatus(status: String?) {
        _listState.update { it.copy(statusFilter = status) }
        applyFilters()
    }

    fun onSearchQueryChange(query: String) {
        _listState.update { it.copy(searchInput = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _listState.update { it.copy(appliedSearchQuery = query) }
            applyFilters()
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _listState.update { it.copy(searchInput = "", appliedSearchQuery = "") }
        applyFilters()
    }

    fun clearListMessages() {
        _listState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun prepareForm(osId: Int? = null) {
        _formState.update { OrdemServicoFormUiState(isLoading = true) }
        viewModelScope.launch {
            val veiculosResult = veiculoRepository.getVeiculos()
            val clientesResult = clienteRepository.getClientes()
            val servicosResult = servicoRepository.getServicos()
            val veiculos = veiculosResult.getOrDefault(emptyList())
            val clientes = clientesResult.getOrDefault(emptyList())
            val servicos = servicosResult.getOrDefault(emptyList())
            val erroCarga = listOf(veiculosResult, clientesResult, servicosResult)
                .filterIsInstance<NetworkResult.Error>()
                .firstOrNull()?.message

            if (osId == null || osId == 0) {
                originalStatus = null
                val firstVeiculo = veiculos.firstOrNull()
                val firstServico = servicos.firstOrNull()
                _formState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = erroCarga,
                        veiculos = veiculos,
                        clientes = clientes,
                        todosServicos = servicos,
                        selectedVeiculoId = firstVeiculo?.id,
                        selectedServicoId = firstServico?.id,
                        valorCobradoInput = firstServico?.precoPadrao?.toString() ?: "0.0"
                    )
                }
            } else {
                when (val result = ordemServicoRepository.getOrdemById(osId)) {
                    is NetworkResult.Success -> {
                        val d = result.data
                        originalStatus = d.os.status
                        _formState.update {
                            it.copy(
                                id = d.os.id,
                                isLoading = false,
                                errorMessage = erroCarga,
                                veiculos = veiculos,
                                clientes = clientes,
                                todosServicos = servicos,
                                selectedVeiculoId = d.os.veiculoId,
                                status = d.os.status,
                                observacoesInput = d.os.observacoes,
                                itens = d.itens,
                                selectedServicoId = servicos.firstOrNull()?.id,
                                valorCobradoInput = servicos.firstOrNull()?.precoPadrao?.toString() ?: "0.0"
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun onVeiculoSelected(id: Int) = _formState.update { it.copy(selectedVeiculoId = id) }
    fun onStatusSelected(status: String) = _formState.update { it.copy(status = status) }
    fun onObservacoesChange(obs: String) = _formState.update { it.copy(observacoesInput = obs) }

    fun openAddItemDialog() {
        val servico = _formState.value.todosServicos.find { it.id == _formState.value.selectedServicoId }
            ?: _formState.value.todosServicos.firstOrNull()

        _formState.update {
            it.copy(
                isAddItemDialogOpen = true,
                selectedServicoId = servico?.id,
                quantidadeInput = "1",
                valorCobradoInput = servico?.precoPadrao?.toString() ?: "0.0",
                errorMessage = null
            )
        }
    }

    fun closeAddItemDialog() {
        _formState.update { it.copy(isAddItemDialogOpen = false) }
    }

    fun onServicoSelectedInDialog(servicoId: Int) {
        val servico = _formState.value.todosServicos.find { it.id == servicoId }
        _formState.update {
            it.copy(
                selectedServicoId = servicoId,
                valorCobradoInput = servico?.precoPadrao?.toString() ?: it.valorCobradoInput
            )
        }
    }

    fun onQuantidadeChange(v: String) = _formState.update { it.copy(quantidadeInput = v) }
    fun onValorCobradoChange(v: String) = _formState.update { it.copy(valorCobradoInput = v) }

    fun addItemToOS() {
        val s = _formState.value
        val servicoId = s.selectedServicoId
        val qtd = s.quantidadeInput.toIntOrNull()
        val valor = s.valorCobradoInput.toDoubleOrNull()

        if (servicoId == null) {
            _formState.update { it.copy(errorMessage = "Selecione um serviço.") }
            return
        }
        val validationMessage = FormValidation.validateItem(qtd, valor)
        if (validationMessage != null) {
            _formState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        if (s.itens.any { it.servicoId == servicoId }) {
            _formState.update { it.copy(errorMessage = "Este serviço já foi adicionado. Remova o item para incluir de novo.") }
            return
        }

        val servico = s.todosServicos.find { it.id == servicoId }
        val newItem = OSServicoItem(
            servicoId = servicoId,
            quantidade = qtd!!,
            valorCobrado = valor!!,
            nomeServico = servico?.nome,
            descricaoServico = servico?.descricao
        )

        val updatedList = s.itens.toMutableList().apply { add(newItem) }
        _formState.update {
            it.copy(
                itens = updatedList,
                isAddItemDialogOpen = false,
                errorMessage = null
            )
        }
    }

    fun removeItemFromOS(index: Int) {
        val currentList = _formState.value.itens.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _formState.update { it.copy(itens = currentList) }
        }
    }

    fun saveOrdemServico() {
        val s = _formState.value
        if (s.isSaving) return
        val veiculoId = s.selectedVeiculoId

        val validationMessage = FormValidation.validateOrdem(veiculoId, s.itens.size)
        if (validationMessage != null) {
            _formState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dataHoje = dateFormat.format(Date())

        val ordem = OrdemServico(
            id = s.id,
            veiculoId = veiculoId!!,
            status = s.status,
            dataAbertura = dataHoje,
            observacoes = s.observacoesInput.trim()
        )

        val osIdEditada = s.id
        val statusAnterior = originalStatus

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, isSaving = true) }
            val result = if (s.id == null) {
                ordemServicoRepository.createOrdem(ordem, s.itens)
            } else {
                ordemServicoRepository.updateOrdem(ordem, s.itens)
            }
            when (result) {
                is NetworkResult.Success -> {
                    _formState.update { it.copy(isLoading = false, isSaving = false, isSavedSuccess = true) }
                    loadOrdens()
                    if (osIdEditada != null && s.status != statusAnterior) {
                        originalStatus = s.status
                        notificationHelper?.notificarMudancaStatus(osIdEditada, s.status)
                    }
                }
                is NetworkResult.Error -> {
                    _formState.update { it.copy(isLoading = false, isSaving = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun solicitarExclusao(os: OrdemServico) {
        _listState.update { it.copy(osParaExcluir = os) }
    }

    fun cancelarExclusao() {
        _listState.update { it.copy(osParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val os = _listState.value.osParaExcluir ?: return
        val id = os.id ?: return

        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, osParaExcluir = null) }
            when (val result = ordemServicoRepository.deleteOrdem(id)) {
                is NetworkResult.Success -> {
                    _listState.update { it.copy(successMessage = "OS #${id} excluída com sucesso!") }
                    loadOrdens()
                }
                is NetworkResult.Error -> {
                    _listState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
