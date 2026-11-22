package com.exemplo.oficina.ui.screens

import androidx.compose.runtime.Composable
import com.exemplo.oficina.ui.viewmodel.OrdemServicoViewModel

@Composable
fun OrdemServicoDetailScreen(
    osId: Int,
    viewModel: OrdemServicoViewModel,
    onNavigateBack: () -> Unit
) {
    // A tela de formulário/detalhes unificada permite visualizar, alterar o status,
    // editar itens dinamicamente e salvar as alterações.
    OrdemServicoFormScreen(
        osId = osId,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}
