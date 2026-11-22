<?php

require_once __DIR__ . '/helpers.php';
require_once __DIR__ . '/config.php';

exigirMetodo(array('GET'));
exigirToken($pdo);

try {
    $abertas = $pdo->query("SELECT COUNT(*) AS total FROM ordens_servico WHERE status = 'ABERTA'")->fetch();
    $andamento = $pdo->query("SELECT COUNT(*) AS total FROM ordens_servico WHERE status = 'EM_ANDAMENTO'")->fetch();
    $finalizadasMes = $pdo->query(
        "SELECT COUNT(*) AS total FROM ordens_servico
         WHERE status = 'FINALIZADA'
           AND YEAR(data_abertura) = YEAR(CURRENT_DATE())
           AND MONTH(data_abertura) = MONTH(CURRENT_DATE())"
    )->fetch();
    $finalizadas = $pdo->query("SELECT COUNT(*) AS total FROM ordens_servico WHERE status = 'FINALIZADA'")->fetch();
    $canceladas = $pdo->query("SELECT COUNT(*) AS total FROM ordens_servico WHERE status = 'CANCELADA'")->fetch();

    $faturamento = $pdo->query(
        "SELECT COALESCE(SUM(osv.quantidade * osv.valor_cobrado), 0) AS total
         FROM ordens_servico os
         INNER JOIN os_servicos osv ON osv.os_id = os.id
         WHERE os.status = 'FINALIZADA'
           AND YEAR(os.data_abertura) = YEAR(CURRENT_DATE())
           AND MONTH(os.data_abertura) = MONTH(CURRENT_DATE())"
    )->fetch();

    responderJson(200, array(
        'os_abertas' => (int) $abertas['total'],
        'os_em_andamento' => (int) $andamento['total'],
        'os_finalizadas_mes' => (int) $finalizadasMes['total'],
        'faturamento_mes' => (float) $faturamento['total'],
        'os_finalizadas' => (int) $finalizadas['total'],
        'os_canceladas' => (int) $canceladas['total'],
    ));
} catch (PDOException $exception) {
    responderJson(500, array('erro' => 'Erro ao obter o resumo da oficina.'));
}
