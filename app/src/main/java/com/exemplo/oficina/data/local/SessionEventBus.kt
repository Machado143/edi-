package com.exemplo.oficina.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEventBus {
    private val _sessionExpired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<String> = _sessionExpired.asSharedFlow()

    fun emitSessionExpired(message: String = "Sessão expirada. Faça login novamente.") {
        _sessionExpired.tryEmit(message)
    }
}
