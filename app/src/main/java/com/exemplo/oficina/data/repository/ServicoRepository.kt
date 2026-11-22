package com.exemplo.oficina.data.repository

import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.remote.ServicoDto
import com.exemplo.oficina.domain.model.Servico

class ServicoRepository(private val apiService: ApiService) {

    suspend fun getServicos(): NetworkResult<List<Servico>> {
        return try {
            val response = apiService.getServicos()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { dtoToDomain(it) }
                NetworkResult.Success(list)
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao carregar serviços.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun createServico(servico: Servico): NetworkResult<Servico> {
        return try {
            val dto = domainToDto(servico)
            val response = apiService.createServico(dto)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(servico.copy(id = response.body()!!.id))
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao cadastrar serviço.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun updateServico(servico: Servico): NetworkResult<String> {
        val id = servico.id ?: return NetworkResult.Error("Serviço sem identificação.", null)
        return try {
            val response = apiService.updateServico(id, domainToDto(servico))
            if (response.isSuccessful) {
                NetworkResult.Success("Serviço atualizado com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao atualizar serviço.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun deleteServico(id: Int): NetworkResult<String> {
        return try {
            val response = apiService.deleteServico(id)
            if (response.isSuccessful) {
                NetworkResult.Success("Serviço excluído com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao excluir serviço", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    private fun dtoToDomain(dto: ServicoDto): Servico {
        return Servico(
            id = dto.id,
            nome = dto.nome,
            descricao = dto.descricao ?: "",
            precoPadrao = dto.precoPadrao
        )
    }

    private fun domainToDto(domain: Servico): ServicoDto {
        return ServicoDto(
            id = domain.id,
            nome = domain.nome,
            descricao = domain.descricao,
            precoPadrao = domain.precoPadrao
        )
    }
}
