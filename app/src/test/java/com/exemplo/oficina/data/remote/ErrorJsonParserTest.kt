package com.exemplo.oficina.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ErrorJsonParserTest {
    @Test
    fun extraiErroDeErrorBodyJson() {
        val json = "{\"erro\":\"CPF/CNPJ já cadastrado.\"}"
        assertEquals("CPF/CNPJ já cadastrado.", ErrorJsonParser.extractErrorMessage(json))
    }

    @Test
    fun retornaNullParaJsonSemErro() {
        assertNull(ErrorJsonParser.extractErrorMessage("{\"mensagem\":\"ok\"}"))
    }

    @Test
    fun retornaNullParaJsonInvalido() {
        assertNull(ErrorJsonParser.extractErrorMessage("corpo inválido"))
    }
}
