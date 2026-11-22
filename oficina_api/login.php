<?php

require_once __DIR__ . '/helpers.php';
require_once __DIR__ . '/config.php';

exigirMetodo(array('POST'));
$dados = lerCorpoJson();
$email = textoObrigatorio($dados, 'email');
$senha = textoObrigatorio($dados, 'senha');

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    responderJson(422, array('erro' => 'Informe um e-mail válido.'));
}

try {
    $stmt = $pdo->prepare('SELECT id, nome, email, cargo, senha_hash FROM usuarios WHERE email = ? LIMIT 1');
    $stmt->execute(array($email));
    $usuario = $stmt->fetch();

    if (!$usuario || !password_verify($senha, $usuario['senha_hash'])) {
        responderJson(401, array('erro' => 'E-mail ou senha inválidos.'));
    }

    $token = bin2hex(random_bytes(32));
    $update = $pdo->prepare('UPDATE usuarios SET token = ? WHERE id = ?');
    $update->execute(array($token, $usuario['id']));

    responderJson(200, array(
        'mensagem' => 'Login realizado com sucesso.',
        'token' => $token,
        'usuario' => array(
            'id' => (int) $usuario['id'],
            'nome' => $usuario['nome'],
            'email' => $usuario['email'],
            'cargo' => $usuario['cargo'],
        ),
    ));
} catch (Exception $exception) {
    responderJson(500, array('erro' => 'Erro interno ao realizar o login.'));
}
