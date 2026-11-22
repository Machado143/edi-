package com.exemplo.oficina.domain.model

/**
 * Modelos de Domínio da Oficina Mecânica
 */

data class Usuario(
    val id: Int? = null,
    val nome: String,
    val email: String,
    val cargo: String = "MECANICO" // ADMIN, MECANICO, ATENDENTE
)

data class Cliente(
    val id: Int? = null,
    val nome: String,
    val cpfCnpj: String,
    val telefone: String,
    val email: String = "",
    val endereco: String = ""
)

data class Veiculo(
    val id: Int? = null,
    val clienteId: Int,
    val placa: String,
    val modelo: String,
    val marca: String,
    val ano: Int,
    val nomeCliente: String? = null
)

data class Servico(
    val id: Int? = null,
    val nome: String,
    val descricao: String = "",
    val precoPadrao: Double
)

object StatusOS {
    const val ABERTA = "ABERTA"
    const val EM_ANDAMENTO = "EM_ANDAMENTO"
    const val FINALIZADA = "FINALIZADA"
    const val CANCELADA = "CANCELADA"

    val todos = listOf(ABERTA, EM_ANDAMENTO, FINALIZADA, CANCELADA)
}

data class OrdemServico(
    val id: Int? = null,
    val veiculoId: Int,
    val usuarioId: Int,
    val status: String = StatusOS.ABERTA,
    val dataAbertura: String,
    val observacoes: String = "",
    val placa: String? = null,
    val modelo: String? = null,
    val nomeCliente: String? = null,
    val valorTotal: Double = 0.0
)

data class OSServicoItem(
    val id: Int? = null,
    val osId: Int? = null,
    val servicoId: Int,
    val quantidade: Int = 1,
    val valorCobrado: Double,
    val nomeServico: String? = null,
    val descricaoServico: String? = null
)

data class OrdemServicoDetalhada(
    val os: OrdemServico,
    val cliente: Cliente?,
    val veiculo: Veiculo?,
    val usuario: Usuario?,
    val itens: List<OSServicoItem> = emptyList(),
    val valorTotalCalculado: Double = 0.0
)

data class ResumoDashboard(
    val osAbertas: Int = 0,
    val osEmAndamento: Int = 0,
    val osFinalizadasMes: Int = 0,
    val faturamentoMes: Double = 0.0,
    val osFinalizadas: Int = osFinalizadasMes,
    val osCanceladas: Int = 0
)
