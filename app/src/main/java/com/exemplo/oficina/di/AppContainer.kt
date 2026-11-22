package com.exemplo.oficina.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exemplo.oficina.data.local.SettingsRepository
import com.exemplo.oficina.data.local.UserPreferencesRepository
import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.repository.AuthRepository
import com.exemplo.oficina.data.repository.ClienteRepository
import com.exemplo.oficina.data.repository.DashboardRepository
import com.exemplo.oficina.data.repository.OrdemServicoRepository
import com.exemplo.oficina.data.repository.ServicoRepository
import com.exemplo.oficina.data.repository.VeiculoRepository
import com.exemplo.oficina.ui.viewmodel.ClienteViewModel
import com.exemplo.oficina.ui.viewmodel.DashboardViewModel
import com.exemplo.oficina.ui.viewmodel.LoginViewModel
import com.exemplo.oficina.ui.viewmodel.OrdemServicoViewModel
import com.exemplo.oficina.ui.viewmodel.ServicoViewModel
import com.exemplo.oficina.ui.viewmodel.SettingsViewModel
import com.exemplo.oficina.ui.viewmodel.VeiculoViewModel
import com.exemplo.oficina.ui.util.NotificationHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.exemplo.oficina.data.local.SessionEventBus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import java.net.URI
import java.util.concurrent.TimeUnit

interface AppContainer {
    val userPreferencesRepository: UserPreferencesRepository
    val settingsRepository: SettingsRepository
    val clienteRepository: ClienteRepository
    val veiculoRepository: VeiculoRepository
    val servicoRepository: ServicoRepository
    val ordemServicoRepository: OrdemServicoRepository
    val authRepository: AuthRepository
    val dashboardRepository: DashboardRepository
    val notificationHelper: NotificationHelper
    val apiService: ApiService
    val sessionExpiredEvents: SharedFlow<String>
    fun recreateApiService(baseUrl: String)
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    override val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context, settingsRepository)
    }

    override val sessionExpiredEvents: SharedFlow<String> = SessionEventBus.sessionExpired

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val storedUrl = runBlocking { userPreferencesRepository.baseUrlFlow.first() }
                    .trim().let { if (it.endsWith("/")) it else "$it/" }
                val target = try { URI(storedUrl) } catch (_: Exception) { null }

                var urlBuilder = original.url.newBuilder()
                if (target != null && !target.host.isNullOrBlank()) {
                    val fileName = original.url.pathSegments.lastOrNull().orEmpty()
                    val basePath = target.path.trim('/')
                    val prefix = if (basePath.isBlank()) "" else "/$basePath"
                    val scheme = target.scheme ?: "http"
                    val port = if (target.port > 0) target.port else if (scheme == "https") 443 else 80
                    urlBuilder = urlBuilder
                        .scheme(scheme)
                        .host(target.host)
                        .port(port)
                        .encodedPath("$prefix/$fileName")
                }

                val token = runBlocking { userPreferencesRepository.userTokenFlow.first() }
                val requestBuilder = original.newBuilder().url(urlBuilder.build())
                if (!original.url.encodedPath.endsWith("login.php") && !token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                val response = chain.proceed(requestBuilder.build())
                if (response.code == 401 && !original.url.encodedPath.endsWith("login.php") && !token.isNullOrBlank()) {
                    runBlocking { userPreferencesRepository.clearSession() }
                    SessionEventBus.emitSessionExpired("Sessão expirada. Faça login novamente.")
                }
                response
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private var retrofit: Retrofit = createRetrofit("http://10.0.2.2/oficina_api/")

    override var apiService: ApiService = retrofit.create(ApiService::class.java)
        private set

    override fun recreateApiService(baseUrl: String) {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        retrofit = createRetrofit(normalized)
        apiService = retrofit.create(ApiService::class.java)
        runBlocking { userPreferencesRepository.saveBaseUrl(normalized) }
    }

    override val clienteRepository: ClienteRepository by lazy {
        ClienteRepository(apiService)
    }

    override val veiculoRepository: VeiculoRepository by lazy {
        VeiculoRepository(apiService)
    }

    override val servicoRepository: ServicoRepository by lazy {
        ServicoRepository(apiService)
    }

    override val ordemServicoRepository: OrdemServicoRepository by lazy {
        OrdemServicoRepository(apiService)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, userPreferencesRepository)
    }

    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(apiService)
    }
}

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(container.authRepository, container.userPreferencesRepository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(container.dashboardRepository, container.userPreferencesRepository, container.settingsRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(container.settingsRepository, container.userPreferencesRepository, { container.apiService }, container::recreateApiService) as T
            }
            modelClass.isAssignableFrom(ClienteViewModel::class.java) -> {
                ClienteViewModel(container.clienteRepository) as T
            }
            modelClass.isAssignableFrom(VeiculoViewModel::class.java) -> {
                VeiculoViewModel(container.veiculoRepository, container.clienteRepository) as T
            }
            modelClass.isAssignableFrom(ServicoViewModel::class.java) -> {
                ServicoViewModel(container.servicoRepository) as T
            }
            modelClass.isAssignableFrom(OrdemServicoViewModel::class.java) -> {
                OrdemServicoViewModel(
                    container.ordemServicoRepository,
                    container.veiculoRepository,
                    container.clienteRepository,
                    container.servicoRepository,
                    container.settingsRepository,
                    container.notificationHelper
                ) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}
