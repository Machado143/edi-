package com.exemplo.oficina.data.repository

import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.remote.OSServicoItemDto
import com.exemplo.oficina.data.remote.OrdemServicoDto
import com.exemplo.oficina.data.remote.UpdateStatusRequestDto
import com.exemplo.oficina.domain.model.OSServicoItem
import com.exemplo.oficina.domain.model.OrdemServico
import com.exemplo.oficina.domain.model.OrdemServicoDetalhada
import com.exemplo.oficina.domain.model.StatusOS

class OrdemServicoRepository(private val apiService: ApiService) {

    suspend fun getOrdens(): NetworkResult<List<OrdemServico>> {
        return try {
            val response = apiService.getOrdens()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { dtoToDomain(it) }
                NetworkResult.Success(list)
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao carregar ordens de serviço.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun getOrdemById(osId: Int): NetworkResult<OrdemServicoDetalhada> {
        return try {
            val response = apiService.getOrdemById(osId)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val osDomain = dtoToDomain(dto)
                val itensDomain = dto.itens?.map { itemDtoToDomain(it) } ?: emptyList()

                val detalhada = OrdemServicoDetalhada(
                    os = osDomain,
                    cliente = null,
                    veiculo = null,
                    usuario = null,
                    itens = itensDomain,
                    valorTotalCalculado = itensDomain.sumOf { it.quantidade * it.valorCobrado }
                )
                NetworkResult.Success(detalhada)
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao carregar a ordem de serviço.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun createOrdem(ordem: OrdemServico, itens: List<OSServicoItem>): NetworkResult<String> {
        return try {
            val dto = domainToDto(ordem, itens)
            val response = apiService.createOrdem(dto)
            if (response.isSuccessful) {
                NetworkResult.Success("Ordem de serviço criada com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao criar a ordem de serviço.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun updateOrdem(ordem: OrdemServico, itens: List<OSServicoItem>): NetworkResult<String> {
        val id = ordem.id ?: return NetworkResult.Error("Ordem de serviço sem identificação.", null)
        return try {
            val response = apiService.updateOrdem(id, domainToDto(ordem, itens))
            if (response.isSuccessful) {
                NetworkResult.Success("Ordem de serviço atualizada com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao atualizar a ordem de serviço.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun updateStatus(osId: Int, status: String): NetworkResult<String> {
        return try {
            val response = apiService.updateStatusOrdem(osId, UpdateStatusRequestDto(status))
            if (response.isSuccessful) {
                NetworkResult.Success("Status alterado para $status")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao alterar o status.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun deleteOrdem(id: Int): NetworkResult<String> {
        return try {
            val response = apiService.deleteOrdem(id)
            if (response.isSuccessful) {
                NetworkResult.Success("OS excluída com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao excluir a OS", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    private fun dtoToDomain(dto: OrdemServicoDto): OrdemServico {
        return OrdemServico(
            id = dto.id,
            veiculoId = dto.veiculoId,
            usuarioId = dto.usuarioId ?: 1,
            status = dto.status ?: StatusOS.ABERTA,
            dataAbertura = dto.dataAbertura ?: "2025-02-24",
            observacoes = dto.observacoes ?: "",
            placa = dto.placa,
            modelo = dto.modelo,
            nomeCliente = dto.nomeCliente,
            valorTotal = dto.valorTotal ?: 0.0
        )
    }

    private fun itemDtoToDomain(dto: OSServicoItemDto): OSServicoItem {
        return OSServicoItem(
            id = dto.id,
            osId = dto.osId,
            servicoId = dto.servicoId,
            quantidade = dto.quantidade,
            valorCobrado = dto.valorCobrado,
            nomeServico = dto.nomeServico,
            descricaoServico = dto.descricaoServico
        )
    }

    private fun domainToDto(domain: OrdemServico, itens: List<OSServicoItem>): OrdemServicoDto {
        return OrdemServicoDto(
            id = domain.id,
            veiculoId = domain.veiculoId,
            status = domain.status,
            dataAbertura = domain.dataAbertura,
            observacoes = domain.observacoes,
            itens = itens.map { itemDomainToDto(it) }
        )
    }

    private fun itemDomainToDto(domain: OSServicoItem): OSServicoItemDto {
        return OSServicoItemDto(
            id = domain.id,
            osId = domain.osId,
            servicoId = domain.servicoId,
            quantidade = domain.quantidade,
            valorCobrado = domain.valorCobrado
        )
    }
}
