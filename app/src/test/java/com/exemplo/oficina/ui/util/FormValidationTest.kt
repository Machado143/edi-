package com.exemplo.oficina.ui.util

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FormValidationTest {
    @Test
    fun itemValido() {
        assertNull(FormValidation.validateItem(1, 250.0))
    }

    @Test
    fun quantidadeNaoPodeSerZero() {
        assertNotNull(FormValidation.validateItem(0, 250.0))
    }

    @Test
    fun valorNaoPodeSerNegativo() {
        assertNotNull(FormValidation.validateItem(1, -1.0))
    }

    @Test
    fun ordemPrecisaDeVeiculoEItem() {
        assertNotNull(FormValidation.validateOrdem(null, 1))
        assertNotNull(FormValidation.validateOrdem(1, 0))
        assertNull(FormValidation.validateOrdem(1, 1))
    }

    @Test
    fun cpfCom11Ou14DigitosSemMascara() {
        assertNull(FormValidation.validateCliente("Cliente", "12345678901"))
        assertNull(FormValidation.validateCliente("Empresa", "12345678000190"))
    }

    @Test
    fun cpfCom11Ou14DigitosComMascara() {
        assertNull(FormValidation.validateCliente("Cliente", "123.456.789-01"))
        assertNull(FormValidation.validateCliente("Empresa", "12.345.678/0001-90"))
    }

    @Test
    fun cpfCom20DigitosInvalido() {
        assertNotNull(FormValidation.validateCliente("Cliente", "12345678901234567890"))
    }

    @Test
    fun validaClienteVeiculoEServico() {
        assertNotNull(FormValidation.validateCliente("", "123"))
        assertNotNull(FormValidation.validateVeiculo(null, "ABC1D23", "Civic", 2024))
        assertNotNull(FormValidation.validateServico("", 100.0))
    }

    @Test
    fun veiculo_aceitaPlacasValidas() {
        assertNull(FormValidation.validateVeiculo(1, "ABC1D23", "Civic", 2022))
        assertNull(FormValidation.validateVeiculo(1, "ABC-1234", "Gol", 2010))
    }

    @Test
    fun veiculo_rejeitaPlacaInvalida() {
        assertNotNull(FormValidation.validateVeiculo(1, "AB12", "Civic", 2022))
        assertNotNull(FormValidation.validateVeiculo(1, "1234567", "Civic", 2022))
    }
}
