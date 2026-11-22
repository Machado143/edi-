package com.exemplo.oficina.ui.util

object InputMasks {
    fun phone(value: String): String {
        val digits = value.filter(Char::isDigit).take(11)
        return when {
            digits.length <= 2 -> if (digits.isEmpty()) "" else "($digits"
            digits.length <= 6 -> "(${digits.take(2)}) ${digits.drop(2)}"
            digits.length <= 10 -> "(${digits.take(2)}) ${digits.drop(2).take(4)}-${digits.drop(6)}"
            else -> "(${digits.take(2)}) ${digits.drop(2).take(5)}-${digits.drop(7)}"
        }
    }

    fun plate(value: String): String {
        val normalized = value
            .filter { it.isLetterOrDigit() }
            .take(7)
            .uppercase()

        return if (normalized.length > 3) {
            "${normalized.take(3)}-${normalized.drop(3)}"
        } else {
            normalized
        }
    }
}
