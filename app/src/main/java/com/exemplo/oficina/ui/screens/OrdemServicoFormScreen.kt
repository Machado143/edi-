package com.exemplo.oficina.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemplo.oficina.domain.model.OSServicoItem
import com.exemplo.oficina.domain.model.StatusOS
import com.exemplo.oficina.ui.viewmodel.OrdemServicoFormUiState
import com.exemplo.oficina.ui.viewmodel.OrdemServicoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdemServicoFormScreen(
    osId: Int? = null,
    viewModel: OrdemServicoViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(osId) {
        viewModel.prepareForm(osId)
    }

    LaunchedEffect(state.isSavedSuccess) {
        if (state.isSavedSuccess) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var veiculoExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val veiculoSelecionado = state.veiculos.find { it.id == state.selectedVeiculoId }
    val clienteDoVeiculo = state.clientes.find { it.id == veiculoSelecionado?.clienteId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == null) "Nova Ordem de Serviço" else "Editar OS #${state.id}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dropdown Seleção de Veículo (Placa + Nome do Cliente)
                ExposedDropdownMenuBox(
                    expanded = veiculoExpanded,
                    onExpandedChange = { veiculoExpanded = !veiculoExpanded }
                ) {
                    OutlinedTextField(
                        value = if (veiculoSelecionado != null) "${veiculoSelecionado.modelo} [${veiculoSelecionado.placa}] - ${veiculoSelecionado.nomeCliente ?: clienteDoVeiculo?.nome ?: ""}" else "Selecione o Veículo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Veículo (Placa + Cliente) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = veiculoExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = veiculoExpanded,
                        onDismissRequest = { veiculoExpanded = false }
                    ) {
                        state.veiculos.forEach { veiculo ->
                            val owner = veiculo.nomeCliente ?: state.clientes.find { it.id == veiculo.clienteId }?.nome ?: ""
                            DropdownMenuItem(
                                text = { Text("${veiculo.modelo} [${veiculo.placa}] - $owner") },
                                onClick = {
                                    veiculo.id?.let { viewModel.onVeiculoSelected(it) }
                                    veiculoExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown de Status da OS
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = state.status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status da OS *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        StatusOS.todos.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    viewModel.onStatusSelected(st)
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Campo Observações
                OutlinedTextField(
                    value = state.observacoesInput,
                    onValueChange = viewModel::onObservacoesChange,
                    label = { Text("Observações / Problemas relatados") },
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )

                // Cabeçalho dos Serviços com Recálculo em Tempo Real
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Itens de Serviço (${state.itens.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.forLanguageTag("pt-BR"), "Total: R$ %.2f", state.valorTotalCalculado),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedButton(
                    onClick = viewModel::openAddItemDialog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Adicionar Item de Serviço")
                }

                // Lista Dinâmica de Itens
                if (state.itens.isEmpty()) {
                    Text(
                        text = "Nenhum item adicionado. Adicione pelo menos 1 item.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.itens) { index, item ->
                            OSFormItemCard(
                                item = item,
                                onDelete = { viewModel.removeItemFromOS(index) }
                            )
                        }
                    }
                }

                Button(
                    onClick = viewModel::saveOrdemServico,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Text("Salvar Ordem de Serviço", fontSize = 16.sp)
                }
            }
        }
    }

    // Modal para Adicionar Item
    if (state.isAddItemDialogOpen) {
        AddItemDialog(
            state = state,
            viewModel = viewModel
        )
    }
}

@Composable
fun OSFormItemCard(
    item: OSServicoItem,
    onDelete: () -> Unit
) {
    val totalItem = item.quantidade * item.valorCobrado

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nomeServico ?: "Serviço #${item.servicoId}", fontWeight = FontWeight.Bold)
                Text(
                    text = String.format(Locale.forLanguageTag("pt-BR"), "Qtd: %d x R$ %.2f = R$ %.2f", item.quantidade, item.valorCobrado, totalItem),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    state: OrdemServicoFormUiState,
    viewModel: OrdemServicoViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val servicoSelecionado = state.todosServicos.find { it.id == state.selectedServicoId }

    AlertDialog(
        onDismissRequest = viewModel::closeAddItemDialog,
        title = { Text("Adicionar Item de Serviço") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = servicoSelecionado?.nome ?: "Selecione o Serviço",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Serviço *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.todosServicos.forEach { servico ->
                            DropdownMenuItem(
                                text = { Text("${servico.nome} - R$ ${servico.precoPadrao}") },
                                onClick = {
                                    servico.id?.let { viewModel.onServicoSelectedInDialog(it) }
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.quantidadeInput,
                    onValueChange = viewModel::onQuantidadeChange,
                    label = { Text("Quantidade * (deve ser > 0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.valorCobradoInput,
                    onValueChange = viewModel::onValorCobradoChange,
                    label = { Text("Valor Cobrado (R$) * (deve ser >= 0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                state.errorMessage?.let { msg ->
                    Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::addItemToOS) {
                Text("Adicionar Item")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeAddItemDialog) {
                Text("Cancelar")
            }
        }
    )
}
