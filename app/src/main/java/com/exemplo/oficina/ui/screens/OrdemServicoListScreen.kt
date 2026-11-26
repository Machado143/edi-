package com.exemplo.oficina.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exemplo.oficina.ui.components.StatusBadgeComponent
import com.exemplo.oficina.ui.components.appCardColors
import com.exemplo.oficina.ui.components.appCardBorder
import com.exemplo.oficina.ui.components.appTopBarColors
import androidx.compose.ui.unit.sp
import com.exemplo.oficina.domain.model.OrdemServico
import com.exemplo.oficina.domain.model.StatusOS
import com.exemplo.oficina.ui.viewmodel.OrdemServicoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdemServicoListScreen(
    viewModel: OrdemServicoViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToForm: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadOrdens()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearListMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearListMessages()
        }
    }

    val osFiltradas = state.filteredOrdens

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ordens de Serviço") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = appTopBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToForm) {
                Icon(Icons.Default.Add, contentDescription = "Abrir Nova OS")
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
            OutlinedTextField(
                value = state.searchInput,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                label = { Text("Buscar por placa ou cliente") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchInput.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips de Filtro por Status
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.statusFilter == null,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text("Todas (${state.ordens.size})") }
                    )
                }
                items(StatusOS.todos) { status ->
                    val count = state.ordens.count { it.status == status }
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { viewModel.filterByStatus(status) },
                        label = { Text("$status ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Carregando ordens de serviço...")
                }
            }
            // Empty
            else if (osFiltradas.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Nenhuma ordem de serviço encontrada.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            // LazyColumn
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(osFiltradas, key = { it.id ?: 0 }) { os ->
                        OrdemServicoCardItem(
                            os = os,
                            onClick = { os.id?.let(onNavigateToDetail) },
                            onDelete = { viewModel.solicitarExclusao(os) }
                        )
                    }
                }
            }
        }
    }

    // Modal de Confirmação de Exclusão da OS
    state.osParaExcluir?.let { os ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,
            title = { Text("Excluir Ordem de Serviço") },
            text = { Text("Deseja realmente excluir a OS #${os.id} (${os.modelo ?: "Veículo"} - ${os.placa ?: ""})?") },
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
fun OrdemServicoCardItem(
    os: OrdemServico,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = appCardColors(),
        border = appCardBorder(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OS #${os.id ?: "-"}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadgeChip(status = os.status)
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir OS", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${os.modelo ?: "Veículo"} | Placa: ${os.placa ?: "---"}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Cliente: ${os.nomeCliente ?: "Cliente Desconhecido"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Abertura: ${os.dataAbertura}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = String.format(Locale.forLanguageTag("pt-BR"), "Total: R$ %.2f", os.valorTotal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatusBadgeChip(status: String) {
    StatusBadgeComponent(status = status)
}
