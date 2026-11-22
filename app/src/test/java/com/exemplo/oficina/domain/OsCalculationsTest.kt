package com.exemplo.oficina.domain

import com.exemplo.oficina.domain.model.OSServicoItem
import com.exemplo.oficina.domain.model.OsCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class OsCalculationsTest {
    @Test
    fun calculaTotalComQuantidade() {
        val itens = listOf(
            OSServicoItem(servicoId = 1, quantidade = 2, valorCobrado = 250.0),
            OSServicoItem(servicoId = 2, quantidade = 1, valorCobrado = 180.0)
        )

        assertEquals(680.0, OsCalculations.calcularTotal(itens), 0.0001)
    }

    @Test
    fun listaVaziaTemTotalZero() {
        assertEquals(0.0, OsCalculations.calcularTotal(emptyList()), 0.0001)
    }
}
