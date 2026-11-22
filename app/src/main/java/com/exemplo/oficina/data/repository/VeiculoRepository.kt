package com.exemplo.oficina.data.repository

import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.remote.VeiculoDto
import com.exemplo.oficina.domain.model.Veiculo
import com.google.gson.Gson
import com.google.gson.JsonObject

class VeiculoRepository(private val apiService: ApiService) {

    suspend fun getVeiculos(): NetworkResult<List<Veiculo>> {
        return try {
            val response = apiService.getVeiculos()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { dtoToDomain(it) }
                NetworkResult.Success(list)
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao carregar veículos.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun createVeiculo(veiculo: Veiculo): NetworkResult<Veiculo> {
        return try {
            val dto = domainToDto(veiculo)
            val response = apiService.createVeiculo(dto)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(veiculo.copy(id = response.body()!!.id))
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao cadastrar veículo.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun updateVeiculo(veiculo: Veiculo): NetworkResult<String> {
        val id = veiculo.id ?: return NetworkResult.Error("Veículo sem identificação.", null)
        return try {
            val response = apiService.updateVeiculo(id, domainToDto(veiculo))
            if (response.isSuccessful) {
                NetworkResult.Success("Veículo atualizado com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao atualizar veículo.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun deleteVeiculo(id: Int): NetworkResult<String> {
        return try {
            val response = apiService.deleteVeiculo(id)
            if (response.isSuccessful) {
                NetworkResult.Success("Veículo excluído com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao excluir veículo", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    private fun dtoToDomain(dto: VeiculoDto): Veiculo {
        return Veiculo(
            id = dto.id,
            clienteId = dto.clienteId,
            placa = dto.placa,
            modelo = dto.modelo,
            marca = dto.marca ?: "",
            ano = dto.ano ?: 2024,
            nomeCliente = dto.nomeCliente
        )
    }

    private fun domainToDto(domain: Veiculo): VeiculoDto {
        return VeiculoDto(
            id = domain.id,
            clienteId = domain.clienteId,
            placa = domain.placa,
            modelo = domain.modelo,
            marca = domain.marca,
            ano = domain.ano,
            nomeCliente = domain.nomeCliente
        )
    }
}
