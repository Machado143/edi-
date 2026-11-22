package com.exemplo.oficina.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Interface Retrofit contendo todos os endpoints do contrato da API REST.
 */
interface ApiService {

    // 1. Autenticação
    @POST("login.php")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    // 2. Clientes
    @GET("clientes.php")
    suspend fun getClientes(): Response<List<ClienteDto>>

    @GET("clientes.php")
        suspend fun getClienteById(@Query("id") id: Int): Response<ClienteDto>

        @POST("clientes.php")
    suspend fun createCliente(@Body cliente: ClienteDto): Response<MessageResponseDto>

    @PUT("clientes.php")
        suspend fun updateCliente(@Query("id") id: Int, @Body cliente: ClienteDto): Response<MessageResponseDto>

        @DELETE("clientes.php")
        suspend fun deleteCliente(@Query("id") id: Int): Response<MessageResponseDto>

    // 3. Veículos (Retorna nome_cliente via JOIN no backend)
    @GET("veiculos.php")
    suspend fun getVeiculos(): Response<List<VeiculoDto>>

    @GET("veiculos.php")
        suspend fun getVeiculoById(@Query("id") id: Int): Response<VeiculoDto>

        @POST("veiculos.php")
    suspend fun createVeiculo(@Body veiculo: VeiculoDto): Response<MessageResponseDto>

    @PUT("veiculos.php")
        suspend fun updateVeiculo(@Query("id") id: Int, @Body veiculo: VeiculoDto): Response<MessageResponseDto>

        @DELETE("veiculos.php")
        suspend fun deleteVeiculo(@Query("id") id: Int): Response<MessageResponseDto>

    // 4. Serviços
    @GET("servicos.php")
    suspend fun getServicos(): Response<List<ServicoDto>>

    @GET("servicos.php")
        suspend fun getServicoById(@Query("id") id: Int): Response<ServicoDto>

        @POST("servicos.php")
    suspend fun createServico(@Body servico: ServicoDto): Response<MessageResponseDto>

    @PUT("servicos.php")
        suspend fun updateServico(@Query("id") id: Int, @Body servico: ServicoDto): Response<MessageResponseDto>

        @DELETE("servicos.php")
        suspend fun deleteServico(@Query("id") id: Int): Response<MessageResponseDto>

    // 5. Ordens de Serviço (GET /ordens retorna placa, modelo, nome_cliente e total calculado)
    @GET("ordens.php")
    suspend fun getOrdens(): Response<List<OrdemServicoDto>>

    @GET("ordens.php")
        suspend fun getOrdemById(@Query("id") id: Int): Response<OrdemServicoDto>

        @POST("ordens.php")
    suspend fun createOrdem(@Body ordem: OrdemServicoDto): Response<MessageResponseDto>

    @PUT("ordens.php")
        suspend fun updateOrdem(@Query("id") id: Int, @Body ordem: OrdemServicoDto): Response<MessageResponseDto>

        @PATCH("ordens.php")
        suspend fun updateStatusOrdem(@Query("id") id: Int, @Body statusReq: UpdateStatusRequestDto): Response<MessageResponseDto>

        @DELETE("ordens.php")
        suspend fun deleteOrdem(@Query("id") id: Int): Response<MessageResponseDto>

    // 6. Resumo / Analytics
    @GET("resumo.php")
    suspend fun getResumo(): Response<ResumoDashboardDto>
}
