package com.exemplo.oficina.data.repository

/**
 * Sealed class para encapsular respostas de rede e tratamento de erros.
 */
fun apiError(response: retrofit2.Response<*>): String? {
    return try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else com.exemplo.oficina.data.remote.ErrorJsonParser.extractErrorMessage(body)
    } catch (_: Exception) {
        null
    }
}

fun connectionOrServerMessage(exception: Exception): String {
    return if (exception is java.net.UnknownHostException ||
        exception is java.net.ConnectException ||
        exception is java.net.SocketTimeoutException
    ) "Servidor inacessível" else "Erro de comunicação com o servidor."
}

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()

    fun getOrDefault(defaultValue: @UnsafeVariance T): T {
        return when (this) {
            is Success -> data
            else -> defaultValue
        }
    }

    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }
}
