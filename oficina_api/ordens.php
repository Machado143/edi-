<?php

require_once __DIR__ . '/helpers.php';
require_once __DIR__ . '/config.php';

exigirMetodo(array('GET', 'POST', 'PUT', 'PATCH', 'DELETE'));
$usuario = exigirToken($pdo);
$metodo = $_SERVER['REQUEST_METHOD'];

function buscarOrdemListaPorId($pdo, $id)
{
    $stmt = $pdo->prepare(
        'SELECT os.id, os.veiculo_id, os.usuario_id, os.status, os.data_abertura, os.observacoes,
                v.placa, v.modelo, c.nome AS nome_cliente,
                COALESCE(SUM(osv.quantidade * osv.valor_cobrado), 0) AS valor_total
         FROM ordens_servico os
         INNER JOIN veiculos v ON v.id = os.veiculo_id
         INNER JOIN clientes c ON c.id = v.cliente_id
         LEFT JOIN os_servicos osv ON osv.os_id = os.id
         WHERE os.id = ?
         GROUP BY os.id, os.veiculo_id, os.usuario_id, os.status, os.data_abertura,
                  os.observacoes, v.placa, v.modelo, c.nome
         LIMIT 1'
    );
    $stmt->execute(array($id));
    return $stmt->fetch();
}

function validarItens($pdo, $itens)
{
    if (!is_array($itens) || count($itens) === 0) {
        responderJson(422, array('erro' => 'Informe pelo menos um item em itens[].'));
    }

    $validados = array();
    $servicosUsados = array();
    foreach ($itens as $item) {
        if (!is_array($item)) {
            responderJson(422, array('erro' => 'Cada item da OS deve ser um objeto JSON.'));
        }
        $servicoId = inteiro(isset($item['servico_id']) ? $item['servico_id'] : null, 'servico_id');
        $quantidade = inteiro(isset($item['quantidade']) ? $item['quantidade'] : null, 'quantidade');
        if (!isset($item['valor_cobrado']) || !is_numeric($item['valor_cobrado']) || (float) $item['valor_cobrado'] < 0) {
            responderJson(422, array('erro' => 'O campo valor_cobrado deve ser maior ou igual a zero.'));
        }

        if (in_array($servicoId, $servicosUsados, true)) {
            responderJson(422, array('erro' => 'O mesmo serviço não pode aparecer duas vezes na OS.'));
        }
        $servicosUsados[] = $servicoId;

        $check = $pdo->prepare('SELECT id FROM servicos WHERE id = ? LIMIT 1');
        $check->execute(array($servicoId));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Um serviço informado não foi encontrado.'));
        }

        $validados[] = array(
            'servico_id' => $servicoId,
            'quantidade' => $quantidade,
            'valor_cobrado' => (float) $item['valor_cobrado'],
        );
    }
    return $validados;
}

try {
    if ($metodo === 'GET') {
        $id = obterIdQuery();
        if ($id !== null) {
            $ordem = buscarOrdemListaPorId($pdo, $id);
            if (!$ordem) {
                responderJson(404, array('erro' => 'Ordem de serviço não encontrada.'));
            }

            $itensStmt = $pdo->prepare(
                'SELECT osv.id, osv.os_id, osv.servico_id, osv.quantidade, osv.valor_cobrado,
                        s.nome AS nome_servico, s.descricao AS descricao_servico
                 FROM os_servicos osv
                 INNER JOIN servicos s ON s.id = osv.servico_id
                 WHERE osv.os_id = ? ORDER BY osv.id ASC'
            );
            $itensStmt->execute(array($id));
            $ordem['itens'] = $itensStmt->fetchAll();
            $ordem['valor_total'] = (float) $ordem['valor_total'];
            responderJson(200, $ordem);
        }

        $stmt = $pdo->query(
            'SELECT os.id, os.veiculo_id, os.usuario_id, os.status, os.data_abertura, os.observacoes,
                    v.placa, v.modelo, c.nome AS nome_cliente,
                    COALESCE(SUM(osv.quantidade * osv.valor_cobrado), 0) AS valor_total
             FROM ordens_servico os
             INNER JOIN veiculos v ON v.id = os.veiculo_id
             INNER JOIN clientes c ON c.id = v.cliente_id
             LEFT JOIN os_servicos osv ON osv.os_id = os.id
             GROUP BY os.id, os.veiculo_id, os.usuario_id, os.status, os.data_abertura,
                      os.observacoes, v.placa, v.modelo, c.nome
             ORDER BY os.data_abertura DESC'
        );
        $ordens = $stmt->fetchAll();
        foreach ($ordens as &$ordem) {
            $ordem['valor_total'] = (float) $ordem['valor_total'];
            $ordem['id'] = (int) $ordem['id'];
        }
        unset($ordem);
        responderJson(200, $ordens);
    }

    if ($metodo === 'POST' || $metodo === 'PUT') {
        $dados = lerCorpoJson();
        $veiculoId = inteiro(isset($dados['veiculo_id']) ? $dados['veiculo_id'] : null, 'veiculo_id');
        $usuarioId = isset($dados['usuario_id']) ? inteiro($dados['usuario_id'], 'usuario_id') : (int) $usuario['id'];
        $status = isset($dados['status']) ? strtoupper(trim((string) $dados['status'])) : 'ABERTA';
        $observacoes = isset($dados['observacoes']) ? trim((string) $dados['observacoes']) : '';
        $permitidos = array('ABERTA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA');
        if (!in_array($status, $permitidos, true)) {
            responderJson(422, array('erro' => 'Status inválido. Use ABERTA, EM_ANDAMENTO, FINALIZADA ou CANCELADA.'));
        }

        $veiculo = $pdo->prepare('SELECT id FROM veiculos WHERE id = ? LIMIT 1');
        $veiculo->execute(array($veiculoId));
        if (!$veiculo->fetch()) {
            responderJson(404, array('erro' => 'Veículo não encontrado.'));
        }

        $usuarioCheck = $pdo->prepare('SELECT id FROM usuarios WHERE id = ? LIMIT 1');
        $usuarioCheck->execute(array($usuarioId));
        if (!$usuarioCheck->fetch()) {
            responderJson(404, array('erro' => 'Usuário responsável não encontrado.'));
        }

        $itens = validarItens($pdo, isset($dados['itens']) ? $dados['itens'] : null);

        $pdo->beginTransaction();
        if ($metodo === 'POST') {
            $insert = $pdo->prepare(
                'INSERT INTO ordens_servico (veiculo_id, usuario_id, status, data_abertura, observacoes)
                 VALUES (?, ?, ?, NOW(), ?)'
            );
            $insert->execute(array($veiculoId, $usuarioId, $status, $observacoes));
            $osId = (int) $pdo->lastInsertId();
        } else {
            $osId = obterIdQuery();
            if ($osId === null) {
                $pdo->rollBack();
                responderJson(400, array('erro' => 'Informe o id da ordem na query string.'));
            }

            $check = $pdo->prepare('SELECT id FROM ordens_servico WHERE id = ? LIMIT 1');
            $check->execute(array($osId));
            if (!$check->fetch()) {
                $pdo->rollBack();
                responderJson(404, array('erro' => 'Ordem de serviço não encontrada.'));
            }

            $update = $pdo->prepare(
                'UPDATE ordens_servico SET veiculo_id = ?, usuario_id = ?, status = ?, observacoes = ? WHERE id = ?'
            );
            $update->execute(array($veiculoId, $usuarioId, $status, $observacoes, $osId));
            $deleteItens = $pdo->prepare('DELETE FROM os_servicos WHERE os_id = ?');
            $deleteItens->execute(array($osId));
        }

        $insertItem = $pdo->prepare(
            'INSERT INTO os_servicos (os_id, servico_id, quantidade, valor_cobrado)
             VALUES (?, ?, ?, ?)'
        );
        foreach ($itens as $item) {
            $insertItem->execute(array(
                $osId,
                $item['servico_id'],
                $item['quantidade'],
                $item['valor_cobrado'],
            ));
        }

        $pdo->commit();
        responderJson($metodo === 'POST' ? 201 : 200, array(
            'id' => $osId,
            'mensagem' => $metodo === 'POST' ? 'Ordem de serviço criada com sucesso.' : 'Ordem de serviço atualizada com sucesso.',
        ));
    }

    if ($metodo === 'PATCH') {
        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id da ordem na query string.'));
        }
        $dados = lerCorpoJson();
        $status = isset($dados['status']) ? strtoupper(trim((string) $dados['status'])) : '';
        $permitidos = array('ABERTA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA');
        if (!in_array($status, $permitidos, true)) {
            responderJson(422, array('erro' => 'Status inválido. Use ABERTA, EM_ANDAMENTO, FINALIZADA ou CANCELADA.'));
        }

        $check = $pdo->prepare('SELECT id FROM ordens_servico WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Ordem de serviço não encontrada.'));
        }

        $update = $pdo->prepare('UPDATE ordens_servico SET status = ? WHERE id = ?');
        $update->execute(array($status, $id));
        responderJson(200, array('mensagem' => 'Status alterado para ' . $status . '.'));
    }

    if ($metodo === 'DELETE') {
        $id = obterIdQuery();
        if ($id === null) {
            responderJson(400, array('erro' => 'Informe o id da ordem na query string.'));
        }

        $check = $pdo->prepare('SELECT id FROM ordens_servico WHERE id = ? LIMIT 1');
        $check->execute(array($id));
        if (!$check->fetch()) {
            responderJson(404, array('erro' => 'Ordem de serviço não encontrada.'));
        }

        $pdo->beginTransaction();
        $delete = $pdo->prepare('DELETE FROM ordens_servico WHERE id = ?');
        $delete->execute(array($id));
        $pdo->commit();
        responderJson(200, array('mensagem' => 'Ordem de serviço excluída com sucesso.'));
    }
} catch (PDOException $exception) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    responderJson(500, array('erro' => 'Erro ao processar a operação de ordens de serviço.'));
}
