package com.exemplo.oficina.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exemplo.oficina.ui.components.appCardColors
import com.exemplo.oficina.ui.components.appCardBorder
import com.exemplo.oficina.ui.components.appTopBarColors
import com.exemplo.oficina.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações do Aplicativo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = appTopBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tema do Aplicativo (Claro / Escuro / Sistema)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = appCardColors(),
                border = appCardBorder(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tema Visual (Aplicado em Tempo Real)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = state.themeMode == "SISTEMA",
                            onClick = { viewModel.setThemeMode("SISTEMA") },
                            label = { Text("Sistema") }
                        )
                        FilterChip(
                            selected = state.themeMode == "CLARO",
                            onClick = { viewModel.setThemeMode("CLARO") },
                            label = { Text("Claro") }
                        )
                        FilterChip(
                            selected = state.themeMode == "ESCURO",
                            onClick = { viewModel.setThemeMode("ESCURO") },
                            label = { Text("Escuro") }
                        )
                    }
                }
            }

            // 2. Nome de Exibição
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = appCardColors(),
                border = appCardBorder(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Perfil do Usuário", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = viewModel::setDisplayName,
                        label = { Text("Nome de Exibição") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // 3. Notificações (Switch)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificações locais", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Avisar quando o status de uma OS for alterado neste aparelho", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                }
            }

            // 4. URL do servidor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = appCardColors(),
                border = appCardBorder(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Servidor da API", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    var urlText by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("URL base") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.saveAndTestConnection(urlText) },
                        enabled = !state.isTesting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isTesting) CircularProgressIndicator() else Text("Salvar e testar conexão")
                    }
                    state.testMessage?.let { message ->
                        Text(message, color = if (message.contains("sucesso", ignoreCase = true) || message.contains("bem-sucedida", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                }
            }

            // 5. Ordenação Padrão da Lista de OS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = appCardColors(),
                border = appCardBorder(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ordenação Padrão da Lista de OS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = state.defaultOsSort == "DATA",
                            onClick = { viewModel.setDefaultOsSort("DATA") },
                            label = { Text("Data") }
                        )
                        FilterChip(
                            selected = state.defaultOsSort == "CLIENTE",
                            onClick = { viewModel.setDefaultOsSort("CLIENTE") },
                            label = { Text("Cliente") }
                        )
                        FilterChip(
                            selected = state.defaultOsSort == "STATUS",
                            onClick = { viewModel.setDefaultOsSort("STATUS") },
                            label = { Text("Status") }
                        )
                    }
                }
            }
        }
    }
}
