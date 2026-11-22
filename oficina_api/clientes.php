<?php

require_once __DIR__ . '/helpers.php';
require_once __DIR__ . '/config.php';

exigirMetodo(array('GET', 'POST', 'PUT', 'DELETE'));
exigirToken($pdo);

$metodo = $_SERVER['REQUEST_METHOD'];

try {
    if ($metodo === 'GET') {
        $id = obterIdQuery();
        if ($id !== null) {
            $stmt = $pdo->prepare('SELECT id, nome, cpf_cnpj, telefone, email, endereco FROM clientes WHERE id = ? LIMIT 1');
            $stmt->execute(array($id));
            $cliente = $stmt->fetch();
            if (!$cliente) {
                responderJson(404, array('erro' => 'Cliente não encontrado.'));
            }
            responderJson(200, $cliente);
        }

        $stmt = $pdo->query('SELECT id, nome, cpf_cnpj, telefone, email, endereco FROM clientes ORDER BY nome ASC');
        responderJson(200, $stmt->fetchAll());
    }

    if ($metodo === 'POST' || $metodo === 'PUT') {
        $dados = lerCorpoJson();
        $nome = textoObrigatorio($dados, 'nome');
        $cpfCnpj = textoObrigatorio($dados, 'cpf_cnpj');
        $cpfCnpj = preg_replace('/[^0-9]/', '', $cpfCnpj);
        if (strlen($cpfCnpj) !== 11 && strlen($cpfCnpj) !== 14) {
            responderJson(422, array('erro' => 'CPF/CNPJ deve conter exatamente 11 ou 14 dígitos numéricos.'));
        }
        $telefone = isset($dados['telefone']) ? trim((string) $dados['telefone']) : '';
        $email = isset($dados['email']) ? trim((string) $dados['email']) : '';
        $endereco = isset($dados['endereco']) ? trim((string) $dados['endereco']) : '';

        if ($email !== '' && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            responderJson(422, array('erro' => 'Informe um e-mail válido.'));
        }

        if ($metodo === 'POST') {
            $duplicado = $pdo->prepare('SELECT id FROM clientes WHERE cpf_cnpj = ? LIMIT 1');
            $duplicado->execute(array($cpfCnpj));
            if ($duplicado->fetch()) {
                responderJson(409, array('erro' => 'CPF/CNPJ já cadastrado.'));
            }

            $stmt = $pdo->prepare('INSERT INTO clientes (nome, cpf_cnpj, telefone, email, endereco) VALUES (?, ?, ?, ?, ?)');
            $stmt->execute(array($nome, $cpfCnpj, $telefone, $email, $endereco));
            $id = (int) $pdo->lastInsertId();
            responderJson(201, array('id' => $id, 'mensagem' => 'Cliente cadastrado com sucesso.'));
        }

        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id do cliente na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM clientes WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Cliente não encontrado.'));
        }

        $duplicado = $pdo->prepare('SELECT id FROM clientes WHERE cpf_cnpj = ? AND id <> ? LIMIT 1');
        $duplicado->execute(array($cpfCnpj, $id));
        if ($duplicado->fetch()) {
            responderJson(409, array('erro' => 'CPF/CNPJ já cadastrado.'));
        }

        $stmt = $pdo->prepare('UPDATE clientes SET nome = ?, cpf_cnpj = ?, telefone = ?, email = ?, endereco = ? WHERE id = ?');
        $stmt->execute(array($nome, $cpfCnpj, $telefone, $email, $endereco, $id));
        responderJson(200, array('mensagem' => 'Cliente atualizado com sucesso.'));
    }

    if ($metodo === 'DELETE') {
        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id do cliente na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM clientes WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Cliente não encontrado.'));
        }

        $veiculos = $pdo->prepare('SELECT COUNT(*) AS total FROM veiculos WHERE cliente_id = ?');
        $veiculos->execute(array($id));
        $resultado = $veiculos->fetch();
        if ((int) $resultado['total'] > 0) {
            responderJson(409, array('erro' => 'Não é possível excluir o cliente porque existem veículos vinculados.'));
        }

        $delete = $pdo->prepare('DELETE FROM clientes WHERE id = ?');
        $delete->execute(array($id));
        responderJson(200, array('mensagem' => 'Cliente excluído com sucesso.'));
    }
} catch (PDOException $exception) {
    if ($exception->getCode() === '23000') {
        responderJson(409, array('erro' => 'Existe conflito com dados já cadastrados.'));
    }
    responderJson(500, array('erro' => 'Erro ao processar a operação de clientes.'));
}
