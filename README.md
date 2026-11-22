# Oficina Mecânica — API PHP e aplicativo Android

## Visão geral

Sistema de gestão de oficina mecânica: cadastro de clientes, veículos, serviços e ordens de serviço (OS), com autenticação, resumo financeiro e configurações. Backend em PHP + MySQL; frontend Android (Kotlin, Compose, MVVM, Retrofit, DataStore).

## Como funciona

1. Usuário faz login (email/senha) → API retorna token Bearer.
2. App armazena token e URL base no DataStore.
3. Telas listam dados via Retrofit (GET *.php) e permitem CRUD.
4. OS vincula veículo + serviços; valor total calculado pela API.
5. Interceptor injeta Authorization em todas as chamadas (exceto login.php) e trata 401 limpando sessão.
6. Configurações (tema, nome, notificações, ordenação, URL) persistem em DataStore.

## Ambiente e pré-requisitos

- Windows com WampServer (Apache, PHP 7.4 ou superior e MySQL/MariaDB).
- Android Studio para executar o aplicativo.
- Emulador Android ou aparelho na mesma rede do servidor.
- Banco MySQL compatível com MySQL 5.7/MariaDB 10.x.

## Instalar o banco

1. Abra `http://localhost/phpmyadmin`.
2. Clique em **Importar**.
3. Selecione `database/oficina.sql` deste projeto.
4. Execute.

O script cria o banco `oficina`, as seis tabelas solicitadas, o campo `usuarios.token`, índices, relacionamentos e dados de exemplo.

O usuário inicial é:

- E-mail: `admin@oficina.com`
- Senha: `123456`

A senha existe somente no comentário do SQL e no texto desta documentação. No banco é salvo apenas o hash bcrypt gerado com `password_hash`.

## Instalar a API no Wamp

1. Copie a pasta `oficina_api` para:

```text
C:\wamp64\www\oficina_api
```

2. Confirme que `config.php` usa:

```php
$host = '127.0.0.1';
$dbname = 'oficina';
$username = 'root';
$password = '';
```

3. Reinicie os serviços Apache e MySQL pelo WampServer.
4. Teste no navegador:

```text
http://localhost/oficina_api/resumo.php
```

Sem token, a resposta esperada é HTTP 401 em JSON.

No Android, ajuste o contrato e use esta URL base no emulador:

```text
http://10.0.2.2/oficina_api/
```

Em aparelho físico, use o endereço IPv4 do computador na rede local:

```text
http://192.168.1.10/oficina_api/
```

O caminho atual do Retrofit do app é `AppContainer.kt`. Como esta API não usa rotas ou diretórios virtuais, a base deve apontar para a pasta e os métodos Retrofit devem usar `?id=`.

## Apache e o header Authorization

A API verifica `HTTP_AUTHORIZATION`, `REDIRECT_HTTP_AUTHORIZATION` e `getallheaders()`. Normalmente o header chega no Apache/Wamp. Caso o PHP não receba `Authorization`, habilite o módulo `mod_rewrite` não é necessário para esta API, mas em alguns Apache pode ser necessário passar o header no `httpd.conf` ou no VirtualHost:

```apache
SetEnvIf Authorization "(.+)" HTTP_AUTHORIZATION=$1
```

Não crie `.htaccess`; todas as chamadas são feitas diretamente a arquivos PHP.

## Contrato final da API

| Arquivo | Método | Parâmetros `?id=` | Corpo JSON | Resposta de exemplo |
|---|---|---|---|---|
| `login.php` | POST | nenhum | `{ "email":"admin@oficina.com", "senha":"123456" }` | 200: `{ "usuario":{ "id":1,"nome":"Administrador Oficina","email":"admin@oficina.com","cargo":"ADMIN" }, "token":"...", "mensagem":"Login realizado com sucesso." }` |
| `clientes.php` | GET | opcional | nenhum | 200: lista ou objeto de clientes |
| `clientes.php` | POST | nenhum | `{ "nome":"Ana","cpf_cnpj":"123","telefone":"...","email":"ana@x.com","endereco":"Rua A" }` | 201: `{ "id":1,"mensagem":"Cliente cadastrado com sucesso." }` |
| `clientes.php` | PUT | obrigatório | mesmos campos do cadastro | 200: `{ "mensagem":"Cliente atualizado com sucesso." }` |
| `clientes.php` | DELETE | obrigatório | nenhum | 200 ou 409 com mensagem em português |
| `veiculos.php` | GET | opcional | nenhum | 200: veículo(s) com `nome_cliente` |
| `veiculos.php` | POST | nenhum | `{ "cliente_id":1,"placa":"ABC1D23","modelo":"Civic","marca":"Honda","ano":2022 }` | 201: `{ "id":1,"mensagem":"Veículo cadastrado com sucesso." }` |
| `veiculos.php` | PUT | obrigatório | mesmos campos do cadastro | 200: `{ "mensagem":"Veículo atualizado com sucesso." }` |
| `veiculos.php` | DELETE | obrigatório | nenhum | 200 ou 409 |
| `servicos.php` | GET | opcional | nenhum | 200: lista ou objeto de serviços |
| `servicos.php` | POST | nenhum | `{ "nome":"Troca de óleo","descricao":"...","preco_padrao":250 }` | 201: `{ "id":1,"mensagem":"Serviço cadastrado com sucesso." }` |
| `servicos.php` | PUT | obrigatório | mesmos campos | 200: `{ "mensagem":"Serviço atualizado com sucesso." }` |
| `servicos.php` | DELETE | obrigatório | nenhum | 200 ou 409 |
| `ordens.php` | GET | opcional | nenhum | lista com `placa`, `modelo`, `nome_cliente`, `valor_total`; com `?id=` inclui `itens[]` |
| `ordens.php` | POST | nenhum | `{ "veiculo_id":1,"usuario_id":1,"status":"ABERTA","observacoes":"","itens":[{"servico_id":1,"quantidade":1,"valor_cobrado":250},{"servico_id":3,"quantidade":1,"valor_cobrado":450}] }` | 201: `{ "id":1,"mensagem":"Ordem de serviço criada com sucesso." }` |
| `ordens.php` | PUT | obrigatório | mesma estrutura do POST | 200: `{ "mensagem":"Ordem de serviço atualizada com sucesso." }` |
| `ordens.php` | PATCH | obrigatório | `{ "status":"EM_ANDAMENTO" }` | 200: `{ "mensagem":"Status alterado para EM_ANDAMENTO." }` |
| `ordens.php` | DELETE | obrigatório | nenhum | 200 ou 404 |
| `resumo.php` | GET | nenhum | nenhum | 200: `{ "os_abertas":1,"os_em_andamento":1,"os_finalizadas_mes":1,"faturamento_mes":100.00,"os_finalizadas":2,"os_canceladas":1 }` |

Todos os endpoints protegidos exigem:

```text
Authorization: Bearer SEU_TOKEN
```

Apenas `login.php` não exige token.

## Comandos curl no Windows

Execute no PowerShell. Use `curl.exe` para evitar o alias do PowerShell.

### 1. Login

```powershell
$login = curl.exe -s -X POST "http://localhost/oficina_api/login.php" `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@oficina.com","senha":"123456"}'
$login
$token = (ConvertFrom-Json $login).token
```

### 2. Listar clientes com token

```powershell
curl.exe -i -X GET "http://localhost/oficina_api/clientes.php" `
  -H "Authorization: Bearer $token"
```

### 3. Criar OS com dois itens

```powershell
curl.exe -i -X POST "http://localhost/oficina_api/ordens.php" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"veiculo_id":1,"usuario_id":1,"status":"ABERTA","observacoes":"Teste pelo curl","itens":[{"servico_id":1,"quantidade":1,"valor_cobrado":250},{"servico_id":3,"quantidade":1,"valor_cobrado":450}]}'
```

### 4. Buscar OS por id

Substitua `7` pelo id retornado na criação:

```powershell
curl.exe -i -X GET "http://localhost/oficina_api/ordens.php?id=7" `
  -H "Authorization: Bearer $token"
```

### 5. Alterar status com PATCH

```powershell
curl.exe -i -X PATCH "http://localhost/oficina_api/ordens.php?id=7" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"status":"EM_ANDAMENTO"}'
```

### 6. Excluir cliente com veículo: 409

```powershell
curl.exe -i -X DELETE "http://localhost/oficina_api/clientes.php?id=1" `
  -H "Authorization: Bearer $token"
```

### 7. Chamada sem token: 401

```powershell
curl.exe -i -X GET "http://localhost/oficina_api/clientes.php"
```

## Códigos HTTP

- `200` — operação concluída.
- `201` — recurso criado.
- `400` — parâmetro obrigatório ausente ou método inválido.
- `401` — token ausente, inválido ou expirado.
- `404` — recurso não encontrado.
- `409` — conflito ou exclusão bloqueada por relacionamento.
- `422` — validação de dados.
- `500` — erro interno do servidor.

Os erros são retornados em JSON no formato `{ "erro":"Mensagem em português" }`.

## Aplicativo Android

Stack: Kotlin, Jetpack Compose, Material 3, MVVM, Navigation Compose, StateFlow, Retrofit, DataStore.

Camadas: `data/remote` (ApiService, DTOs), `data/repository` (um repositório por recurso), `data/local` (DataStore), `domain/model`, `ui/screens`, `ui/viewmodel`, `ui/navigation`, `di` (AppContainer manual).

### Executar

1. Importar `database/oficina.sql` e copiar `oficina_api/` para a pasta web (ver seções acima).
2. Abrir o projeto no Android Studio e executar em emulador. URL padrão: `http://10.0.2.2/oficina_api/`.
3. Em celular físico, informar `http://IP_DO_PC/oficina_api/` na tela de Configurações (engrenagem na tela de login).

### Usuários de exemplo

| Uso | E-mail | Senha |
|---|---|---|
| App | admin@oficina.com | 123456 |
| Testes automatizados | teste@oficina.com | teste123 |
| Reserva | demo@oficina.com | demo123 |

Cada usuário tem um token próprio. Um novo login do mesmo usuário invalida o token anterior.

### Testes

- API: `powershell -ExecutionPolicy Bypass -File .\tests\api-tests.ps1` (parâmetros `-BaseUrl`, `-Email`, `-Senha`).
- Unitários: `.\gradlew.bat :app:testDebugUnitTest` (requer `JAVA_HOME` do JDK do Android Studio).
- Interface: teste manual no emulador.

### APK

Android Studio: Build > Build Bundle(s) / APK(s) > Build APK(s).
