package com.exemplo.oficina.data.repository

import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.domain.model.ResumoDashboard

class DashboardRepository(private val apiService: ApiService) {

    suspend fun getResumo(): NetworkResult<ResumoDashboard> {
        return try {
            val response = apiService.getResumo()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val resumo = ResumoDashboard(
                    osAbertas = dto.osAbertas,
                    osEmAndamento = dto.osEmAndamento,
                    osFinalizadasMes = dto.osFinalizadasMes,
                    faturamentoMes = dto.faturamentoMes,
                    osFinalizadas = dto.osFinalizadas,
                    osCanceladas = dto.osCanceladas
                )
                NetworkResult.Success(resumo)
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao carregar o resumo.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }
}
