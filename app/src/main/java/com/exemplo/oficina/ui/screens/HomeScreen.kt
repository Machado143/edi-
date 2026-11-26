package com.exemplo.oficina.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemplo.oficina.ui.components.IconBadge
import com.exemplo.oficina.ui.components.appCardBorder
import com.exemplo.oficina.ui.components.appCardColors
import com.exemplo.oficina.ui.components.appTopBarColors
import com.exemplo.oficina.ui.components.topBarContentColor
import com.exemplo.oficina.ui.theme.StatusAbertaColor
import com.exemplo.oficina.ui.theme.StatusAndamentoColor
import com.exemplo.oficina.ui.theme.StatusCanceladaColor
import com.exemplo.oficina.ui.theme.StatusFinalizadaColor
import com.exemplo.oficina.ui.viewmodel.DashboardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DashboardViewModel,
    onNavigateToClientes: () -> Unit,
    onNavigateToVeiculos: () -> Unit,
    onNavigateToServicos: () -> Unit,
    onNavigateToOrdensServico: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Olá, ${state.usuarioNome}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Oficina Mecânica · ${state.usuarioCargo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = topBarContentColor().copy(alpha = 0.8f)
                        )
                    }
                },
                colors = appTopBarColors(),
                actions = {
                    IconButton(onClick = viewModel::loadDashboard) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToOrdensServico() },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Ordens") },
                    label = { Text("Ordens") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToClientes() },
                    icon = { Icon(Icons.Default.People, contentDescription = "Clientes") },
                    label = { Text("Clientes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToVeiculos() },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Veículos") },
                    label = { Text("Veículos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToServicos() },
                    icon = { Icon(Icons.Default.Work, contentDescription = "Serviços") },
                    label = { Text("Serviços") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Resumo geral",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Carregando estatísticas...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Cards de resumo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResumoCard(
                            title = "OS Abertas",
                            value = "${state.resumo.osAbertas}",
                            icon = Icons.Default.PendingActions,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ResumoCard(
                            title = "Em Andamento",
                            value = "${state.resumo.osEmAndamento}",
                            icon = Icons.Default.Build,
                            color = StatusAndamentoColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResumoCard(
                            title = "Finalizadas (Mês)",
                            value = "${state.resumo.osFinalizadasMes}",
                            icon = Icons.Default.CheckCircle,
                            color = StatusFinalizadaColor,
                            modifier = Modifier.weight(1f)
                        )
                        ResumoCard(
                            title = "Faturamento (Mês)",
                            value = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", state.resumo.faturamentoMes),
                            icon = Icons.Default.AttachMoney,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OrdemStatusBarChart(resumo = state.resumo)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Módulos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloMenuCard(
                            title = "Ordens de Serviço",
                            subtitle = "Gerenciar ordens e itens",
                            icon = Icons.Default.ListAlt,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onNavigateToOrdensServico,
                            modifier = Modifier.weight(1f)
                        )
                        ModuloMenuCard(
                            title = "Clientes",
                            subtitle = "Gerenciar clientes",
                            icon = Icons.Default.People,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = onNavigateToClientes,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloMenuCard(
                            title = "Veículos",
                            subtitle = "Cadastro de veículos",
                            icon = Icons.Default.DirectionsCar,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = onNavigateToVeiculos,
                            modifier = Modifier.weight(1f)
                        )
                        ModuloMenuCard(
                            title = "Serviços",
                            subtitle = "Catálogo de serviços",
                            icon = Icons.Default.Work,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = onNavigateToServicos,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrdemStatusBarChart(resumo: com.exemplo.oficina.domain.model.ResumoDashboard) {
    val finalizadas = maxOf(resumo.osFinalizadas, resumo.osFinalizadasMes)
    val items = listOf(
        Triple("Abertas", resumo.osAbertas, StatusAbertaColor),
        Triple("Andamento", resumo.osEmAndamento, StatusAndamentoColor),
        Triple("Finalizadas", finalizadas, StatusFinalizadaColor),
        Triple("Canceladas", resumo.osCanceladas, StatusCanceladaColor)
    )
    val maxCount = maxOf(items.maxOf { it.second }, 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appCardBorder(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ordens de serviço por status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val gap = 18.dp.toPx()
                val barWidth = (size.width - gap * (items.size - 1)) / items.size
                val chartHeight = size.height - 18.dp.toPx()

                items.forEachIndexed { index, (_, count, color) ->
                    val barHeight = maxOf(chartHeight * count.toFloat() / maxCount.toFloat(), 4.dp.toPx())
                    val left = index * (barWidth + gap)
                    val top = size.height - barHeight

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEach { (label, count, color) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("$count", fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
                        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ResumoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appCardBorder(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            IconBadge(icon = icon, tint = color)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ModuloMenuCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
