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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.exemplo.oficina.ui.components.IconBadge
import com.exemplo.oficina.ui.components.appCardColors
import com.exemplo.oficina.ui.components.appCardBorder
import com.exemplo.oficina.ui.components.appTopBarColors
import com.exemplo.oficina.domain.model.Veiculo
import com.exemplo.oficina.ui.viewmodel.VeiculoUiState
import com.exemplo.oficina.ui.viewmodel.VeiculoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculoScreen(
    viewModel: VeiculoViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        if (!state.isFormOpen) {
            state.errorMessage?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessages()
            }
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciamento de Veículos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = appTopBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openForm() }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Veículo")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.veiculos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Nenhum veículo cadastrado.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                // Tabela veiculos exibida via LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.veiculos, key = { it.id ?: 0 }) { veiculo ->
                        val clienteNome = state.clientes.find { it.id == veiculo.clienteId }?.nome ?: "Cliente #${veiculo.clienteId}"
                        VeiculoCardItem(
                            veiculo = veiculo,
                            clienteNome = clienteNome,
                            onEdit = { viewModel.openForm(veiculo) },
                            onDelete = { viewModel.solicitarExclusao(veiculo) }
                        )
                    }
                }
            }
        }
    }

    if (state.isFormOpen) {
        VeiculoFormDialog(state = state, viewModel = viewModel)
    }

    // AlertDialog de Confirmação de Exclusão
    state.veiculoParaExcluir?.let { veiculo ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir o veículo \"${veiculo.placa}\"?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarExclusao) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarExclusao) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VeiculoCardItem(
    veiculo: Veiculo,
    clienteNome: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appCardBorder(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Default.DirectionsCar, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${veiculo.modelo} (${veiculo.marca})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "Placa: ${veiculo.placa} | Ano: ${veiculo.ano}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Proprietário: $clienteNome", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculoFormDialog(
    state: VeiculoUiState,
    viewModel: VeiculoViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val clienteSelecionado = state.clientes.find { it.id == state.clienteIdInput }

    AlertDialog(
        onDismissRequest = viewModel::closeForm,
        title = {
            Text(if (state.selectedVeiculo == null) "Novo Veículo" else "Editar Veículo")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Dropdown Seleção de Cliente (FK cliente_id)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = clienteSelecionado?.nome ?: "Selecione o proprietário",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente Proprietário (FK cliente_id) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.clientes.forEach { cliente ->
                            DropdownMenuItem(
                                text = { Text("${cliente.nome} (${cliente.cpfCnpj})") },
                                onClick = {
                                    cliente.id?.let { viewModel.onClienteSelected(it) }
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.placaInput,
                    onValueChange = viewModel::onPlacaChange,
                    label = { Text("Placa do Veículo *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.modeloInput,
                    onValueChange = viewModel::onModeloChange,
                    label = { Text("Modelo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.marcaInput,
                    onValueChange = viewModel::onMarcaChange,
                    label = { Text("Marca / Fabricante") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.anoInput,
                    onValueChange = viewModel::onAnoChange,
                    label = { Text("Ano de Fabricação *") },
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
            TextButton(onClick = viewModel::saveVeiculo, enabled = !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeForm) {
                Text("Cancelar")
            }
        }
    )
}
