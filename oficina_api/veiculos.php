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
            $stmt = $pdo->prepare(
                'SELECT v.id, v.cliente_id, v.placa, v.modelo, v.marca, v.ano, c.nome AS nome_cliente
                 FROM veiculos v INNER JOIN clientes c ON c.id = v.cliente_id
                 WHERE v.id = ? LIMIT 1'
            );
            $stmt->execute(array($id));
            $veiculo = $stmt->fetch();
            if (!$veiculo) {
                responderJson(404, array('erro' => 'Veículo não encontrado.'));
            }
            responderJson(200, $veiculo);
        }

        $stmt = $pdo->query(
            'SELECT v.id, v.cliente_id, v.placa, v.modelo, v.marca, v.ano, c.nome AS nome_cliente
             FROM veiculos v INNER JOIN clientes c ON c.id = v.cliente_id
             ORDER BY v.placa ASC'
        );
        responderJson(200, $stmt->fetchAll());
    }

    if ($metodo === 'POST' || $metodo === 'PUT') {
        $dados = lerCorpoJson();
        $clienteId = inteiro(isset($dados['cliente_id']) ? $dados['cliente_id'] : null, 'cliente_id');
        $placa = strtoupper(str_replace('-', '', textoObrigatorio($dados, 'placa')));
        if (strlen($placa) > 8) {
            responderJson(422, array('erro' => 'Placa deve possuir até 8 caracteres alfanuméricos.'));
        }
        $modelo = textoObrigatorio($dados, 'modelo');
        $marca = isset($dados['marca']) ? trim((string) $dados['marca']) : '';
        $ano = inteiro(isset($dados['ano']) ? $dados['ano'] : null, 'ano');

        if (strlen($placa) < 1) {
            responderJson(422, array('erro' => 'Placa é obrigatória.'));
        }

        $cliente = $pdo->prepare('SELECT id FROM clientes WHERE id = ? LIMIT 1');
        $cliente->execute(array($clienteId));
        if (!$cliente->fetch()) {
            responderJson(404, array('erro' => 'Cliente proprietário não encontrado.'));
        }

        if ($metodo === 'POST') {
            $duplicado = $pdo->prepare('SELECT id FROM veiculos WHERE placa = ? LIMIT 1');
            $duplicado->execute(array($placa));
            if ($duplicado->fetch()) {
                responderJson(409, array('erro' => 'Placa já cadastrada.'));
            }

            $stmt = $pdo->prepare('INSERT INTO veiculos (cliente_id, placa, modelo, marca, ano) VALUES (?, ?, ?, ?, ?)');
            $stmt->execute(array($clienteId, $placa, $modelo, $marca, $ano));
            responderJson(201, array('id' => (int) $pdo->lastInsertId(), 'mensagem' => 'Veículo cadastrado com sucesso.'));
        }

        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id do veículo na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM veiculos WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Veículo não encontrado.'));
        }

        $duplicado = $pdo->prepare('SELECT id FROM veiculos WHERE placa = ? AND id <> ? LIMIT 1');
        $duplicado->execute(array($placa, $id));
        if ($duplicado->fetch()) {
            responderJson(409, array('erro' => 'Placa já cadastrada.'));
        }

        $stmt = $pdo->prepare('UPDATE veiculos SET cliente_id = ?, placa = ?, modelo = ?, marca = ?, ano = ? WHERE id = ?');
        $stmt->execute(array($clienteId, $placa, $modelo, $marca, $ano, $id));
        responderJson(200, array('mensagem' => 'Veículo atualizado com sucesso.'));
    }

    if ($metodo === 'DELETE') {
        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id do veículo na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM veiculos WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Veículo não encontrado.'));
        }

        $os = $pdo->prepare('SELECT COUNT(*) AS total FROM ordens_servico WHERE veiculo_id = ?');
        $os->execute(array($id));
        $resultado = $os->fetch();
        if ((int) $resultado['total'] > 0) {
            responderJson(409, array('erro' => 'Não é possível excluir o veículo porque existem ordens de serviço vinculadas.'));
        }

        $delete = $pdo->prepare('DELETE FROM veiculos WHERE id = ?');
        $delete->execute(array($id));
        responderJson(200, array('mensagem' => 'Veículo excluído com sucesso.'));
    }
} catch (PDOException $exception) {
    if ($exception->getCode() === '23000') {
        responderJson(409, array('erro' => 'Placa já cadastrada ou dados vinculados impedem a operação.'));
    }
    responderJson(500, array('erro' => 'Erro ao processar a operação de veículos.'));
}
