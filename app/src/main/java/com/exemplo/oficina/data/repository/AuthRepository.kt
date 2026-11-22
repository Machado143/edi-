package com.exemplo.oficina.data.repository

import com.exemplo.oficina.data.local.UserPreferencesRepository
import com.exemplo.oficina.data.remote.ApiService
import com.exemplo.oficina.data.remote.LoginRequestDto
import com.exemplo.oficina.domain.model.Usuario

class AuthRepository(
    private val apiService: ApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend fun login(email: String, pass: String, rememberMe: Boolean = true): NetworkResult<Usuario> {
        return try {
            val response = apiService.login(LoginRequestDto(email, pass))
            val body = response.body()
            if (response.isSuccessful && body?.usuario != null && !body.token.isNullOrBlank()) {
                val dto = body.usuario!!
                val user = Usuario(dto.id, dto.nome, dto.email, dto.cargo ?: "MECANICO")
                userPreferencesRepository.saveUserSession(
                    id = user.id ?: 1,
                    name = user.nome,
                    role = user.cargo,
                    token = body.token!!,
                    rememberMe = rememberMe
                )
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error(
                    message = parseApiError(response.errorBody()) ?: body?.erro ?: "Não foi possível realizar o login.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(messageForException(e), code = null)
        }
    }

    private fun parseApiError(errorBody: okhttp3.ResponseBody?): String? {
        if (errorBody == null) return null
        return try {
            val json = com.google.gson.JsonParser.parseString(errorBody.string()).asJsonObject
            json.get("erro")?.takeIf { !it.isJsonNull }?.asString
        } catch (_: Exception) {
            null
        }
    }

    private fun messageForException(exception: Exception): String {
        return if (exception is java.net.UnknownHostException ||
            exception is java.net.ConnectException ||
            exception is java.net.SocketTimeoutException
        ) "Servidor inacessível" else "Erro de comunicação com o servidor."
    }

    suspend fun logout() {
        userPreferencesRepository.clearSession()
    }
}
