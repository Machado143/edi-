package com.exemplo.oficina.ui.util

object FormValidation {
    fun validateItem(quantidade: Int?, valorCobrado: Double?): String? {
        if (quantidade == null || quantidade <= 0) return "Quantidade deve ser maior que zero."
        if (valorCobrado == null || valorCobrado < 0.0) return "Valor cobrado deve ser maior ou igual a zero."
        return null
    }

    fun validateCliente(nome: String, cpfCnpj: String): String? {
        if (nome.trim().isEmpty()) return "Nome é obrigatório."
        val digits = cpfCnpj.filter(Char::isDigit)
        if (digits.length != 11 && digits.length != 14) {
            return "CPF/CNPJ deve conter exatamente 11 ou 14 dígitos."
        }
        return null
    }

    fun validateVeiculo(clienteId: Int?, placa: String, modelo: String, ano: Int?): String? {
        if (clienteId == null) return "Selecione o cliente."
        if (placa.trim().isEmpty()) return "Placa é obrigatória."
        val placaNormalizada = placa.replace("-", "").trim().uppercase()
        if (!Regex("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}").matches(placaNormalizada)) {
            return "Placa inválida. Use o formato ABC1D23 ou ABC-1234."
        }
        if (modelo.trim().isEmpty()) return "Modelo é obrigatório."
        if (ano == null || ano < 1900 || ano > 2100) return "Ano inválido. Informe um ano entre 1900 e 2100."
        return null
    }

    fun validateServico(nome: String, preco: Double?): String? {
        if (nome.trim().isEmpty()) return "Nome é obrigatório."
        if (preco == null || preco < 0.0) return "Preço padrão inválido."
        return null
    }

    fun validateOrdem(veiculoId: Int?, itensCount: Int): String? {
        if (veiculoId == null) return "Selecione um veículo para a Ordem de Serviço."
        if (itensCount <= 0) return "Adicione pelo menos 1 item de serviço à OS."
        return null
    }
}
