package com.exemplo.oficina.domain.model

object OsCalculations {
    fun calcularTotal(itens: List<OSServicoItem>): Double {
        return itens.sumOf { it.quantidade * it.valorCobrado }
    }
}
