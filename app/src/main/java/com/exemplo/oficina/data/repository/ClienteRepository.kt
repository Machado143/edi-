package com.exemplo.oficina.data.repository

import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.remote.ClienteDto
import com.exemplo.oficina.domain.model.Cliente
import com.google.gson.Gson
import com.google.gson.JsonObject

class ClienteRepository(private val apiService: ApiService) {

    suspend fun getClientes(): NetworkResult<List<Cliente>> {
        return try {
            val response = apiService.getClientes()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { dtoToDomain(it) }
                NetworkResult.Success(list)
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao carregar clientes.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun createCliente(cliente: Cliente): NetworkResult<Cliente> {
        return try {
            val dto = domainToDto(cliente)
            val response = apiService.createCliente(dto)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(cliente.copy(id = response.body()!!.id))
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao cadastrar cliente.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun updateCliente(cliente: Cliente): NetworkResult<String> {
        return try {
            val dto = domainToDto(cliente)
            val response = apiService.updateCliente(cliente.id!!, dto)
            if (response.isSuccessful) {
                NetworkResult.Success("Cliente atualizado com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao atualizar cliente.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    suspend fun deleteCliente(id: Int): NetworkResult<String> {
        return try {
            val response = apiService.deleteCliente(id)
            if (response.isSuccessful) {
                NetworkResult.Success("Cliente excluído com sucesso")
            } else {
                NetworkResult.Error(apiError(response) ?: "Erro ao excluir cliente", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(connectionOrServerMessage(e), null)
        }
    }

    private fun dtoToDomain(dto: ClienteDto): Cliente {
        return Cliente(
            id = dto.id,
            nome = dto.nome,
            cpfCnpj = dto.cpfCnpj,
            telefone = dto.telefone ?: "",
            email = dto.email ?: "",
            endereco = dto.endereco ?: ""
        )
    }

    private fun domainToDto(domain: Cliente): ClienteDto {
        return ClienteDto(
            id = domain.id,
            nome = domain.nome,
            cpfCnpj = domain.cpfCnpj,
            telefone = domain.telefone,
            email = domain.email,
            endereco = domain.endereco
        )
    }
}
