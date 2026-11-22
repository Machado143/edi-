CREATE DATABASE IF NOT EXISTS oficina
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE oficina;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    cargo ENUM('ADMIN', 'MECANICO', 'ATENDENTE') NOT NULL DEFAULT 'MECANICO',
    token VARCHAR(64) NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_email (email),
    UNIQUE KEY uq_usuarios_token (token)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clientes (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    cpf_cnpj VARCHAR(18) NOT NULL,
    telefone VARCHAR(20) NULL,
    email VARCHAR(180) NULL,
    endereco VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_clientes_cpf_cnpj (cpf_cnpj)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS veiculos (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    cliente_id INT UNSIGNED NOT NULL,
    placa VARCHAR(8) NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    marca VARCHAR(80) NULL,
    ano SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_veiculos_placa (placa),
    KEY idx_veiculos_cliente (cliente_id),
    CONSTRAINT fk_veiculos_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servicos (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT NULL,
    preco_padrao DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ordens_servico (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    veiculo_id INT UNSIGNED NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    status ENUM('ABERTA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'ABERTA',
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacoes TEXT NULL,
    PRIMARY KEY (id),
    KEY idx_os_veiculo (veiculo_id),
    KEY idx_os_usuario (usuario_id),
    KEY idx_os_status (status),
    KEY idx_os_data_abertura (data_abertura),
    CONSTRAINT fk_os_veiculo
        FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_os_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS os_servicos (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    os_id INT UNSIGNED NOT NULL,
    servico_id INT UNSIGNED NOT NULL,
    quantidade INT UNSIGNED NOT NULL DEFAULT 1,
    valor_cobrado DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_os_servico (os_id, servico_id),
    KEY idx_os_servicos_servico (servico_id),
    CONSTRAINT fk_os_servicos_os
        FOREIGN KEY (os_id) REFERENCES ordens_servico(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_os_servicos_servico
        FOREIGN KEY (servico_id) REFERENCES servicos(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Senha do administrador: 123456
-- O hash abaixo foi gerado por password_hash(..., PASSWORD_BCRYPT).
INSERT INTO usuarios (id, nome, email, senha_hash, cargo, token) VALUES
(1, 'Administrador Oficina', 'admin@oficina.com', '$2b$10$f7JBBGxIXa6uxxVnJK1OFONhd50nRUwWqfN8xQS6w/rBj1lRsfgy.', 'ADMIN', NULL)
ON DUPLICATE KEY UPDATE
nome = VALUES(nome), senha_hash = VALUES(senha_hash), cargo = VALUES(cargo);

INSERT INTO usuarios (nome, email, senha_hash, cargo, token) VALUES
('Usuário Teste', 'teste@oficina.com', '$2y$10$ORRp88F22RWPLPoselywEuzQjrKtDC3nXfXAdMs6iwLKtRtc5DD62', 'ADMIN', NULL),
('Usuário Demo', 'demo@oficina.com', '$2y$10$v7tu62yZMC4i82Vzsb031OylUBPiOo.nzh/rQUKJe7dCenQu6BBnC', 'ADMIN', NULL)
ON DUPLICATE KEY UPDATE
nome = VALUES(nome), senha_hash = VALUES(senha_hash), cargo = VALUES(cargo);

INSERT INTO clientes (id, nome, cpf_cnpj, telefone, email, endereco) VALUES
(1, 'Ana Souza', '12345678901', '(11) 98888-1111', 'ana@email.com', 'Rua das Flores, 100'),
(2, 'Bruno Lima', '23456789012', '(21) 97777-2222', 'bruno@email.com', 'Av. Brasil, 200'),
(3, 'Carla Mendes', '34567890123', '(31) 96666-3333', 'carla@email.com', 'Rua Minas, 300'),
(4, 'Transportadora Silva Ltda', '12345678000190', '(41) 95555-4444', 'frota@silva.com', 'Rua Paraná, 400'),
(5, 'Diego Santos', '45678901234', '(51) 94444-5555', 'diego@email.com', 'Av. Sul, 500')
ON DUPLICATE KEY UPDATE nome = VALUES(nome);

INSERT INTO veiculos (id, cliente_id, placa, modelo, marca, ano) VALUES
(1, 1, 'ABC1D23', 'Civic', 'Honda', 2022),
(2, 1, 'DEF2E34', 'Corolla', 'Toyota', 2021),
(3, 2, 'GHI3F45', 'Gol', 'Volkswagen', 2020),
(4, 3, 'JKL4G56', 'Onix', 'Chevrolet', 2023),
(5, 4, 'MNO5H67', 'FH 540', 'Volvo', 2019),
(6, 4, 'PQR6J78', 'VM 270', 'Volvo', 2018),
(7, 5, 'STU7K89', 'Hilux', 'Toyota', 2022),
(8, 2, 'VWX8L90', 'Uno', 'Fiat', 2017)
ON DUPLICATE KEY UPDATE modelo = VALUES(modelo);

INSERT INTO servicos (id, nome, descricao, preco_padrao) VALUES
(1, 'Troca de óleo', 'Troca de óleo e filtro', 250.00),
(2, 'Alinhamento e balanceamento', 'Alinhamento e balanceamento das rodas', 180.00),
(3, 'Revisão de freios', 'Inspeção e troca de componentes de freio', 450.00),
(4, 'Diagnóstico eletrônico', 'Diagnóstico por scanner', 150.00),
(5, 'Troca de bateria', 'Teste e substituição da bateria', 320.00),
(6, 'Revisão geral', 'Inspeção completa do veículo', 600.00)
ON DUPLICATE KEY UPDATE descricao = VALUES(descricao), preco_padrao = VALUES(preco_padrao);

INSERT INTO ordens_servico (id, veiculo_id, usuario_id, status, data_abertura, observacoes) VALUES
(1, 1, 1, 'EM_ANDAMENTO', '2026-11-20 09:00:00', 'Barulho ao frear'),
(2, 3, 1, 'ABERTA', '2026-11-21 10:30:00', 'Revisão completa'),
(3, 5, 1, 'FINALIZADA', '2026-11-10 14:00:00', 'Troca de óleo da frota'),
(4, 7, 1, 'CANCELADA', '2026-11-18 16:20:00', 'Cliente cancelou'),
(5, 2, 1, 'ABERTA', '2026-11-19 08:15:00', 'Luz do painel acesa'),
(6, 4, 1, 'FINALIZADA', '2026-10-28 11:45:00', 'Alinhamento solicitado')
ON DUPLICATE KEY UPDATE observacoes = VALUES(observacoes);

INSERT INTO os_servicos (os_id, servico_id, quantidade, valor_cobrado) VALUES
(1, 1, 1, 250.00),
(1, 3, 1, 450.00),
(2, 2, 1, 180.00),
(2, 6, 1, 600.00),
(3, 1, 3, 250.00),
(3, 4, 1, 150.00),
(4, 5, 1, 320.00),
(5, 4, 1, 150.00),
(5, 3, 1, 450.00),
(6, 2, 1, 180.00)
ON DUPLICATE KEY UPDATE quantidade = VALUES(quantidade), valor_cobrado = VALUES(valor_cobrado);
