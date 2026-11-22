<?php

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');
header('Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

function responderJson($status, $dados)
{
    http_response_code($status);
    echo json_encode($dados, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function lerCorpoJson()
{
    $raw = file_get_contents('php://input');
    if ($raw === '' || $raw === false) {
        return array();
    }

    $dados = json_decode($raw, true);
    if (!is_array($dados)) {
        responderJson(400, array('erro' => 'O corpo da requisição deve ser um JSON válido.'));
    }
    return $dados;
}

function obterIdQuery()
{
    if (!isset($_GET['id']) || $_GET['id'] === '') {
        return null;
    }

    $id = filter_var($_GET['id'], FILTER_VALIDATE_INT);
    if ($id === false || $id < 1) {
        responderJson(422, array('erro' => 'O parâmetro id deve ser um número inteiro válido.'));
    }
    return $id;
}

function exigirMetodo($metodos)
{
    $metodo = $_SERVER['REQUEST_METHOD'];
    if (!in_array($metodo, $metodos, true)) {
        responderJson(405, array('erro' => 'Método HTTP não permitido.'));
    }
}

function textoObrigatorio($dados, $campo)
{
    if (!isset($dados[$campo]) || trim((string) $dados[$campo]) === '') {
        responderJson(422, array('erro' => 'O campo ' . $campo . ' é obrigatório.'));
    }
    return trim((string) $dados[$campo]);
}

function inteiro($valor, $campo, $permitirZero = false)
{
    if (!is_numeric($valor)) {
        responderJson(422, array('erro' => 'O campo ' . $campo . ' deve ser numérico.'));
    }
    $numero = (int) $valor;
    $minimo = $permitirZero ? 0 : 1;
    if ($numero < $minimo) {
        responderJson(422, array('erro' => 'O campo ' . $campo . ' possui valor inválido.'));
    }
    return $numero;
}

function obterTokenAuthorization()
{
    $authorization = '';

    if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
        $authorization = $_SERVER['HTTP_AUTHORIZATION'];
    } elseif (isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
        $authorization = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
    } elseif (function_exists('getallheaders')) {
        $headers = getallheaders();
        foreach ($headers as $nome => $valor) {
            if (strcasecmp($nome, 'Authorization') === 0) {
                $authorization = $valor;
                break;
            }
        }
    }

    if (preg_match('/^Bearer\s+(.+)$/i', trim($authorization), $matches)) {
        return trim($matches[1]);
    }
    return null;
}

function exigirToken($pdo)
{
    $token = obterTokenAuthorization();
    if ($token === null || $token === '') {
        responderJson(401, array('erro' => 'Token não informado.'));
    }

    $stmt = $pdo->prepare('SELECT id, nome, email, cargo FROM usuarios WHERE token = ? LIMIT 1');
    $stmt->execute(array($token));
    $usuario = $stmt->fetch();

    if (!$usuario) {
        responderJson(401, array('erro' => 'Token inválido ou expirado.'));
    }
    return $usuario;
}
