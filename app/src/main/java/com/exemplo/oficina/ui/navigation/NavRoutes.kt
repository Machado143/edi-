package com.exemplo.oficina.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Início")
    object Clientes : Screen("clientes", "Clientes")
    object Veiculos : Screen("veiculos", "Veículos")
    object Servicos : Screen("servicos", "Serviços")
    object OrdensServico : Screen("ordens", "Ordens de Serviço")
    object OrdemServicoDetail : Screen("ordem_detail/{osId}", "Detalhes da OS") {
        fun createRoute(osId: Int) = "ordem_detail/$osId"
    }
    object OrdemServicoForm : Screen("ordem_form", "Nova Ordem de Serviço")
    object Settings : Screen("configuracoes", "Configurações Server REST")
}
