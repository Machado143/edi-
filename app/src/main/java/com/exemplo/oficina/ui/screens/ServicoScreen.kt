package com.exemplo.oficina.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.exemplo.oficina.ui.components.IconBadge
import com.exemplo.oficina.ui.components.appCardColors
import com.exemplo.oficina.ui.components.appCardBorder
import com.exemplo.oficina.ui.components.appTopBarColors
import com.exemplo.oficina.domain.model.Servico
import com.exemplo.oficina.ui.viewmodel.ServicoUiState
import com.exemplo.oficina.ui.viewmodel.ServicoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicoScreen(
    viewModel: ServicoViewModel,
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
                title = { Text("Catálogo de Serviços") },
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
                Icon(Icons.Default.Add, contentDescription = "Novo Serviço")
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
            } else if (state.servicos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Nenhum serviço cadastrado.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                // Tabela servicos exibida via LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.servicos, key = { it.id ?: 0 }) { servico ->
                        ServicoCardItem(
                            servico = servico,
                            onEdit = { viewModel.openForm(servico) },
                            onDelete = { viewModel.solicitarExclusao(servico) }
                        )
                    }
                }
            }
        }
    }

    if (state.isFormOpen) {
        ServicoFormDialog(state = state, viewModel = viewModel)
    }

    // AlertDialog de Confirmação de Exclusão
    state.servicoParaExcluir?.let { servico ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir o serviço \"${servico.nome}\"?") },
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
fun ServicoCardItem(
    servico: Servico,
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
            IconBadge(icon = Icons.Default.Work, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = servico.nome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = servico.descricao, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", servico.precoPadrao),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
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

@Composable
fun ServicoFormDialog(
    state: ServicoUiState,
    viewModel: ServicoViewModel
) {
    AlertDialog(
        onDismissRequest = viewModel::closeForm,
        title = {
            Text(if (state.selectedServico == null) "Novo Serviço" else "Editar Serviço")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.nomeInput,
                    onValueChange = viewModel::onNomeChange,
                    label = { Text("Nome do Serviço *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.descricaoInput,
                    onValueChange = viewModel::onDescricaoChange,
                    label = { Text("Descrição detalhada") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.precoPadraoInput,
                    onValueChange = viewModel::onPrecoPadraoChange,
                    label = { Text("Preço Padrão (R$) *") },
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
            TextButton(onClick = viewModel::saveServico, enabled = !state.isSaving) {
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
