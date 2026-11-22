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
            $stmt = $pdo->prepare('SELECT id, nome, descricao, preco_padrao FROM servicos WHERE id = ? LIMIT 1');
            $stmt->execute(array($id));
            $servico = $stmt->fetch();
            if (!$servico) {
                responderJson(404, array('erro' => 'Serviço não encontrado.'));
            }
            responderJson(200, $servico);
        }

        $stmt = $pdo->query('SELECT id, nome, descricao, preco_padrao FROM servicos ORDER BY nome ASC');
        responderJson(200, $stmt->fetchAll());
    }

    if ($metodo === 'POST' || $metodo === 'PUT') {
        $dados = lerCorpoJson();
        $nome = textoObrigatorio($dados, 'nome');
        $descricao = isset($dados['descricao']) ? trim((string) $dados['descricao']) : '';
        if (!isset($dados['preco_padrao']) || !is_numeric($dados['preco_padrao']) || (float) $dados['preco_padrao'] < 0) {
            responderJson(422, array('erro' => 'O campo preco_padrao deve ser um valor válido maior ou igual a zero.'));
        }
        $preco = (float) $dados['preco_padrao'];

        if ($metodo === 'POST') {
            $stmt = $pdo->prepare('INSERT INTO servicos (nome, descricao, preco_padrao) VALUES (?, ?, ?)');
            $stmt->execute(array($nome, $descricao, $preco));
            responderJson(201, array('id' => (int) $pdo->lastInsertId(), 'mensagem' => 'Serviço cadastrado com sucesso.'));
        }

        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id do serviço na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM servicos WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Serviço não encontrado.'));
        }

        $stmt = $pdo->prepare('UPDATE servicos SET nome = ?, descricao = ?, preco_padrao = ? WHERE id = ?');
        $stmt->execute(array($nome, $descricao, $preco, $id));
        responderJson(200, array('mensagem' => 'Serviço atualizado com sucesso.'));
    }

    if ($metodo === 'DELETE') {
        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id do serviço na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM servicos WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Serviço não encontrado.'));
        }

        $itens = $pdo->prepare('SELECT COUNT(*) AS total FROM os_servicos WHERE servico_id = ?');
        $itens->execute(array($id));
        $resultado = $itens->fetch();
        if ((int) $resultado['total'] > 0) {
            responderJson(409, array('erro' => 'Não é possível excluir o serviço porque ele está usado em ordens de serviço.'));
        }

        $delete = $pdo->prepare('DELETE FROM servicos WHERE id = ?');
        $delete->execute(array($id));
        responderJson(200, array('mensagem' => 'Serviço excluído com sucesso.'));
    }
} catch (PDOException $exception) {
    responderJson(500, array('erro' => 'Erro ao processar a operação de serviços.'));
}
