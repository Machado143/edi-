package com.exemplo.oficina.data.remote

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class SafeDoubleAdapter : TypeAdapter<Double>() {
    override fun write(out: JsonWriter, value: Double?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): Double {
        return when (reader.peek()) {
            JsonToken.STRING -> reader.nextString().replace(',', '.').toDoubleOrNull() ?: 0.0
            JsonToken.NUMBER -> reader.nextDouble()
            JsonToken.NULL -> {
                reader.nextNull()
                0.0
            }
            else -> {
                reader.skipValue()
                0.0
            }
        }
    }
}

/**
 * Data Transfer Objects (DTOs) correspondentes às respostas JSON da API REST PHP.
 */

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String
)

data class LoginResponseDto(
    @SerializedName("mensagem") val mensagem: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("usuario") val usuario: UsuarioDto? = null,
    @SerializedName("erro") val erro: String? = null
)

data class UsuarioDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("cargo") val cargo: String? = "MECANICO"
)

data class ClienteDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nome") val nome: String,
    @SerializedName("cpf_cnpj") val cpfCnpj: String,
    @SerializedName("telefone") val telefone: String? = "",
    @SerializedName("email") val email: String? = "",
    @SerializedName("endereco") val endereco: String? = ""
)

data class VeiculoDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("cliente_id") val clienteId: Int,
    @SerializedName("placa") val placa: String,
    @SerializedName("modelo") val modelo: String,
    @SerializedName("marca") val marca: String? = "",
    @SerializedName("ano") val ano: Int? = 2024,
    @SerializedName("nome_cliente") val nomeCliente: String? = null
)

data class ServicoDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = "",
    @SerializedName("preco_padrao") val precoPadrao: Double
)

data class OSServicoItemDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("os_id") val osId: Int? = null,
    @SerializedName("servico_id") val servicoId: Int,
    @SerializedName("quantidade") val quantidade: Int = 1,
    @SerializedName("valor_cobrado")
    @JsonAdapter(SafeDoubleAdapter::class)
    val valorCobrado: Double,
    @SerializedName("nome_servico") val nomeServico: String? = null,
    @SerializedName("descricao_servico") val descricaoServico: String? = null
)

data class OrdemServicoDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("veiculo_id") val veiculoId: Int,
    @SerializedName("usuario_id") val usuarioId: Int? = null,
    @SerializedName("status") val status: String? = "ABERTA",
    @SerializedName("data_abertura") val dataAbertura: String? = null,
    @SerializedName("observacoes") val observacoes: String? = "",
    @SerializedName("placa") val placa: String? = null,
    @SerializedName("modelo") val modelo: String? = null,
    @SerializedName("nome_cliente") val nomeCliente: String? = null,
    @SerializedName("valor_total") val valorTotal: Double? = 0.0,
    @SerializedName("itens") val itens: List<OSServicoItemDto>? = null
)

data class ResumoDashboardDto(
    @SerializedName("os_abertas") val osAbertas: Int = 0,
    @SerializedName("os_em_andamento") val osEmAndamento: Int = 0,
    @SerializedName("os_finalizadas_mes") val osFinalizadasMes: Int = 0,
    @SerializedName("faturamento_mes") val faturamentoMes: Double = 0.0,
    @SerializedName("os_finalizadas") val osFinalizadas: Int = osFinalizadasMes,
    @SerializedName("os_canceladas") val osCanceladas: Int = 0
)

data class MessageResponseDto(
    @SerializedName("mensagem") val mensagem: String? = null,
    @SerializedName("erro") val erro: String? = null,
    @SerializedName("id") val id: Int? = null
)

data class UpdateStatusRequestDto(
    @SerializedName("status") val status: String
)
