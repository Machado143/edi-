package com.exemplo.oficina.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.exemplo.oficina.di.AppContainer
import com.exemplo.oficina.di.ViewModelFactory
import com.exemplo.oficina.ui.screens.ClienteScreen
import com.exemplo.oficina.ui.screens.HomeScreen
import com.exemplo.oficina.ui.screens.LoginScreen
import com.exemplo.oficina.ui.screens.OrdemServicoDetailScreen
import com.exemplo.oficina.ui.screens.OrdemServicoFormScreen
import com.exemplo.oficina.ui.screens.OrdemServicoListScreen
import com.exemplo.oficina.ui.screens.ServicoScreen
import com.exemplo.oficina.ui.screens.SettingsScreen
import com.exemplo.oficina.ui.screens.VeiculoScreen
import com.exemplo.oficina.ui.viewmodel.ClienteViewModel
import com.exemplo.oficina.ui.viewmodel.DashboardViewModel
import com.exemplo.oficina.ui.viewmodel.LoginViewModel
import com.exemplo.oficina.ui.viewmodel.OrdemServicoViewModel
import com.exemplo.oficina.ui.viewmodel.ServicoViewModel
import com.exemplo.oficina.ui.viewmodel.SettingsViewModel
import com.exemplo.oficina.ui.viewmodel.VeiculoViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val factory = ViewModelFactory(appContainer)
    val scope = rememberCoroutineScope()

    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val clienteViewModel: ClienteViewModel = viewModel(factory = factory)
    val veiculoViewModel: VeiculoViewModel = viewModel(factory = factory)
    val servicoViewModel: ServicoViewModel = viewModel(factory = factory)
    val ordemServicoViewModel: OrdemServicoViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    LaunchedEffect(appContainer) {
        appContainer.sessionExpiredEvents.collect { message ->
            loginViewModel.setSessionExpiredMessage(message)
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Observar sessão salva na DataStore
    val savedToken by appContainer.userPreferencesRepository.userTokenFlow.collectAsState(initial = null)
    val rememberMe by appContainer.userPreferencesRepository.rememberMeFlow.collectAsState(initial = true)

    val startRoute = Screen.Login.route

    LaunchedEffect(savedToken, rememberMe) {
        if (!savedToken.isNullOrBlank() && rememberMe && navController.currentDestination?.route == Screen.Login.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (savedToken.isNullOrBlank() && navController.currentDestination?.route != Screen.Login.route) {
            loginViewModel.resetState()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    loginViewModel.resetState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Home.route) {
            LaunchedEffect(Unit) { dashboardViewModel.loadDashboard() }
            HomeScreen(
                viewModel = dashboardViewModel,
                onNavigateToClientes = { navController.navigate(Screen.Clientes.route) },
                onNavigateToVeiculos = { navController.navigate(Screen.Veiculos.route) },
                onNavigateToServicos = { navController.navigate(Screen.Servicos.route) },
                onNavigateToOrdensServico = { navController.navigate(Screen.OrdensServico.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    scope.launch {
                        appContainer.userPreferencesRepository.clearSession()
                        loginViewModel.resetState()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Clientes.route) {
            LaunchedEffect(Unit) { clienteViewModel.loadClientes() }
            ClienteScreen(
                viewModel = clienteViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Veiculos.route) {
            LaunchedEffect(Unit) { veiculoViewModel.loadData() }
            VeiculoScreen(
                viewModel = veiculoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Servicos.route) {
            LaunchedEffect(Unit) { servicoViewModel.loadServicos() }
            ServicoScreen(
                viewModel = servicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OrdensServico.route) {
            OrdemServicoListScreen(
                viewModel = ordemServicoViewModel,
                onNavigateToDetail = { osId ->
                    navController.navigate(Screen.OrdemServicoDetail.createRoute(osId))
                },
                onNavigateToForm = {
                    navController.navigate(Screen.OrdemServicoForm.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.OrdemServicoDetail.route,
            arguments = listOf(navArgument("osId") { type = NavType.IntType })
        ) { backStackEntry ->
            val osId = backStackEntry.arguments?.getInt("osId") ?: 0
            OrdemServicoDetailScreen(
                osId = osId,
                viewModel = ordemServicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OrdemServicoForm.route) {
            OrdemServicoFormScreen(
                viewModel = ordemServicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
