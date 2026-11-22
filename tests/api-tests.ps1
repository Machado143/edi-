param(
    [string]$BaseUrl = "http://localhost/oficina_api/",
    [string]$Email = "teste@oficina.com",
    [string]$Senha = "teste123"
)

$ErrorActionPreference = "Stop"
try {
    [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
} catch {}
$BaseUrl = $BaseUrl.TrimEnd("/") + "/"
$passed = 0
$failed = 0
$token = $null
$createdClientId = $null
$createdVehicleId = $null
$createdOsId = $null

function Write-Assert {
    param(
        [string]$Nome,
        [bool]$Condicao,
        [string]$Detalhe = ""
    )

    if ($Condicao) {
        $script:passed++
        Write-Host ("PASS - " + $Nome) -ForegroundColor Green
    } else {
        $script:failed++
        Write-Host ("FAIL - " + $Nome) -ForegroundColor Red
        if ($Detalhe) {
            Write-Host ("       " + $Detalhe) -ForegroundColor Yellow
        }
    }
}

function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [string]$Token = $null
    )

    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        $headers["Authorization"] = "Bearer " + $Token
    }

    $params = @{
        Uri = $Url
        Method = $Method
        Headers = $headers
        ErrorAction = "Stop"
    }

    if ($null -ne $Body) {
        $params["ContentType"] = "application/json; charset=utf-8"
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }

    if ($PSVersionTable.PSVersion.Major -ge 7) {
        $params["SkipHttpErrorCheck"] = $true
    } elseif ($PSVersionTable.PSVersion.Major -lt 6) {
        $params["UseBasicParsing"] = $true
    }

    try {
        $response = Invoke-WebRequest @params
        $statusCode = [int]$response.StatusCode
        $content = $response.Content
    } catch {
        $statusCode = $null
        $content = $null

        if ($_.Exception.Response -ne $null) {
            try {
                $statusCode = [int]$_.Exception.Response.StatusCode
            } catch {}

            try {
                $stream = $_.Exception.Response.GetResponseStream()
                if ($stream -ne $null) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $content = $reader.ReadToEnd()
                    $reader.Dispose()
                    $stream.Dispose()
                }
            } catch {
                $content = $_.ErrorDetails.Message
            }
        }

        if (-not $content) {
            $content = $_.ErrorDetails.Message
        }
    }

    $json = $null
    if (-not [string]::IsNullOrWhiteSpace($content)) {
        try {
            $json = $content | ConvertFrom-Json
        } catch {}
    }

    return [PSCustomObject]@{
        StatusCode = $statusCode
        Content = $content
        Json = $json
    }
}

function Get-JsonError {
    param([object]$Response)

    if ($null -ne $Response.Json -and $null -ne $Response.Json.erro) {
        return [string]$Response.Json.erro
    }
    return [string]$Response.Content
}

function Try-Delete {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Token
    )

    try {
        Invoke-ApiRequest -Method $Method -Url $Url -Token $Token | Out-Null
    } catch {}
}

$stamp = (Get-Date).ToString("yyyyMMddHHmmss")
$random = Get-Random -Minimum 100 -Maximum 999
$cpfDuplicado = $null
$placaUnica = $null

Write-Host "=" * 72
Write-Host "Testes da API: $BaseUrl"
Write-Host "=" * 72

try {
    $login = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "login.php") -Body @{
        email = $Email
        senha = $Senha
    }

    Write-Assert "Login válido" ($login.StatusCode -eq 200 -and $null -ne $login.Json.token) (
        "Status=$($login.StatusCode); conteúdo=$($login.Content)"
    )

    if ($login.StatusCode -eq 200 -and $login.Json.token) {
        $token = [string]$login.Json.token
    }

    $loginInvalido = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "login.php") -Body @{
        email = "invalido@oficina.com"
        senha = "senha-invalida"
    }
    Write-Assert "Login inválido" ($loginInvalido.StatusCode -eq 401) (
        "Status=$($loginInvalido.StatusCode); erro=$(Get-JsonError $loginInvalido)"
    )

    $semTokenClientes = Invoke-ApiRequest -Method "GET" -Url ($BaseUrl + "clientes.php")
    Write-Assert "GET clientes sem token" ($semTokenClientes.StatusCode -eq 401) (
        "Status=$($semTokenClientes.StatusCode); erro=$(Get-JsonError $semTokenClientes)"
    )

    $semTokenResumo = Invoke-ApiRequest -Method "GET" -Url ($BaseUrl + "resumo.php")
    Write-Assert "GET resumo sem token" ($semTokenResumo.StatusCode -eq 401) (
        "Status=$($semTokenResumo.StatusCode); erro=$(Get-JsonError $semTokenResumo)"
    )

    if (-not $token) {
        Write-Assert "Token obtido para os demais testes" $false "O login válido não retornou token."
    } else {
        $clientes = Invoke-ApiRequest -Method "GET" -Url ($BaseUrl + "clientes.php") -Token $token
        Write-Assert "GET clientes com token" (
            $clientes.StatusCode -eq 200 -and $clientes.Json -is [System.Array]
        ) ("Status=$($clientes.StatusCode); conteúdo=$($clientes.Content)")

        $deleteClienteComVeiculo = Invoke-ApiRequest -Method "DELETE" -Url ($BaseUrl + "clientes.php?id=1") -Token $token
        Write-Assert "DELETE cliente 1 com veículos retorna 409" (
            $deleteClienteComVeiculo.StatusCode -eq 409 -and
            ([string](Get-JsonError $deleteClienteComVeiculo)).Contains("existem") -and
            ([string](Get-JsonError $deleteClienteComVeiculo)).Contains("vinculados")
        ) ("Status=$($deleteClienteComVeiculo.StatusCode); erro=$(Get-JsonError $deleteClienteComVeiculo)")

        $cpfDuplicado = ("9" + $stamp + $random).Substring(0, 11)

        $primeiroCpf = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "clientes.php") -Token $token -Body @{
            nome = "Cliente Teste $stamp"
            cpf_cnpj = $cpfDuplicado
            telefone = "11999990000"
            email = "teste.$stamp@example.com"
            endereco = "Rua dos Testes"
        }

        $cpfCriado = ($primeiroCpf.StatusCode -eq 201 -and $null -ne $primeiroCpf.Json.id)
        Write-Assert "Criar cliente CPF único" $cpfCriado ("Status=$($primeiroCpf.StatusCode); conteúdo=$($primeiroCpf.Content)")

        if ($cpfCriado) {
            $createdClientId = [int]$primeiroCpf.Json.id

            $cpf20 = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "clientes.php") -Token $token -Body @{
            nome = "CPF Inválido $stamp"
            cpf_cnpj = "12345678901234567890"
            telefone = "11999990003"
            email = "cpf20.$stamp@example.com"
            endereco = "Rua Inválida"
        }
        Write-Assert "POST CPF/CNPJ com 20 dígitos retorna 422" (
            $cpf20.StatusCode -eq 422 -and
            ([string](Get-JsonError $cpf20)).Contains("11 ou 14")
        ) ("Status=$($cpf20.StatusCode); erro=$(Get-JsonError $cpf20)")

        $cpfDuplicadoPost = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "clientes.php") -Token $token -Body @{
                nome = "Cliente Duplicado $stamp"
                cpf_cnpj = $cpfDuplicado
                telefone = "11999990001"
                email = "duplicado.$stamp@example.com"
                endereco = "Rua Duplicada"
            }
            Write-Assert "POST CPF/CNPJ duplicado retorna 409" (
                $cpfDuplicadoPost.StatusCode -eq 409 -and
                ([string](Get-JsonError $cpfDuplicadoPost)).Contains("CPF/CNPJ") -and
                ([string](Get-JsonError $cpfDuplicadoPost)).Contains("cadastrado")
            ) ("Status=$($cpfDuplicadoPost.StatusCode); erro=$(Get-JsonError $cpfDuplicadoPost)")

            $putMesmoCpf = Invoke-ApiRequest -Method "PUT" -Url ($BaseUrl + "clientes.php?id=$createdClientId") -Token $token -Body @{
                nome = "Cliente Teste Atualizado $stamp"
                cpf_cnpj = $cpfDuplicado
                telefone = "11999990002"
                email = "teste.atualizado.$stamp@example.com"
                endereco = "Rua dos Testes Atualizada"
            }
            Write-Assert "PUT mantendo próprio CPF/CNPJ retorna 200" (
                $putMesmoCpf.StatusCode -eq 200
            ) ("Status=$($putMesmoCpf.StatusCode); conteúdo=$($putMesmoCpf.Content)")

            $placaUnica = ("TEST" + $stamp.Substring(8, 3) + $random.ToString()).Substring(0, 7).ToUpper()

            $veiculoPrimario = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "veiculos.php") -Token $token -Body @{
                cliente_id = $createdClientId
                placa = $placaUnica
                modelo = "Modelo Teste"
                marca = "Marca Teste"
                ano = 2024
            }
            $veiculoCriado = ($veiculoPrimario.StatusCode -eq 201 -and $null -ne $veiculoPrimario.Json.id)
            Write-Assert "Criar veículo placa única" $veiculoCriado ("Status=$($veiculoPrimario.StatusCode); conteúdo=$($veiculoPrimario.Content)")

            if ($veiculoCriado) {
                $createdVehicleId = [int]$veiculoPrimario.Json.id

                $placaDuplicada = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "veiculos.php") -Token $token -Body @{
                    cliente_id = $createdClientId
                    placa = $placaUnica
                    modelo = "Modelo Duplicado"
                    marca = "Marca Duplicada"
                    ano = 2023
                }
                Write-Assert "POST placa duplicada retorna 409" (
                    $placaDuplicada.StatusCode -eq 409 -and
                    ([string](Get-JsonError $placaDuplicada)).Contains("Placa") -and
                    ([string](Get-JsonError $placaDuplicada)).Contains("cadastrada")
                ) ("Status=$($placaDuplicada.StatusCode); erro=$(Get-JsonError $placaDuplicada)")

                $osRepetida = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "ordens.php") -Token $token -Body @{
                    veiculo_id = $createdVehicleId
                    status = "ABERTA"
                    observacoes = "teste servico repetido"
                    itens = @(
                        @{ servico_id = 1; quantidade = 1; valor_cobrado = 50.00 },
                        @{ servico_id = 1; quantidade = 2; valor_cobrado = 60.00 }
                    )
                }
                Write-Assert "POST OS com servico repetido retorna 422" (
                    $osRepetida.StatusCode -eq 422 -and ([string](Get-JsonError $osRepetida)).Contains("duas vezes")
                ) ("Status=$($osRepetida.StatusCode); erro=$(Get-JsonError $osRepetida)")

                $osNegativa = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "ordens.php") -Token $token -Body @{
                    veiculo_id = $createdVehicleId
                    status = "ABERTA"
                    itens = @( @{ servico_id = 1; quantidade = 1; valor_cobrado = -5 } )
                }
                Write-Assert "POST OS com valor negativo retorna 422" ($osNegativa.StatusCode -eq 422) (
                    "Status=$($osNegativa.StatusCode); erro=$(Get-JsonError $osNegativa)"
                )

                $osSemItens = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "ordens.php") -Token $token -Body @{
                    veiculo_id = $createdVehicleId
                    status = "ABERTA"
                    itens = @()
                }
                Write-Assert "POST OS sem itens retorna 422" ($osSemItens.StatusCode -eq 422) (
                    "Status=$($osSemItens.StatusCode); erro=$(Get-JsonError $osSemItens)"
                )

                $criarOs = Invoke-ApiRequest -Method "POST" -Url ($BaseUrl + "ordens.php") -Token $token -Body @{
                    veiculo_id = $createdVehicleId
                    status = "ABERTA"
                    observacoes = "OS criada pelo teste automatizado $stamp"
                    itens = @(
                        @{ servico_id = 1; quantidade = 1; valor_cobrado = 250.00 },
                        @{ servico_id = 2; quantidade = 2; valor_cobrado = 180.00 }
                    )
                }

                $osCriada = ($criarOs.StatusCode -eq 201 -and $null -ne $criarOs.Json.id)
                Write-Assert "Criar OS com 2 itens" $osCriada ("Status=$($criarOs.StatusCode); conteúdo=$($criarOs.Content)")

                if ($osCriada) {
                    $createdOsId = [int]$criarOs.Json.id

                    $delVeiculoComOs = Invoke-ApiRequest -Method "DELETE" -Url ($BaseUrl + "veiculos.php?id=$createdVehicleId") -Token $token
                    Write-Assert "DELETE veiculo com OS retorna 409" ($delVeiculoComOs.StatusCode -eq 409) (
                        "Status=$($delVeiculoComOs.StatusCode); erro=$(Get-JsonError $delVeiculoComOs)"
                    )

                    $delServicoUsado = Invoke-ApiRequest -Method "DELETE" -Url ($BaseUrl + "servicos.php?id=1") -Token $token
                    Write-Assert "DELETE servico usado em OS retorna 409" ($delServicoUsado.StatusCode -eq 409) (
                        "Status=$($delServicoUsado.StatusCode); erro=$(Get-JsonError $delServicoUsado)"
                    )

                    $buscarOs = Invoke-ApiRequest -Method "GET" -Url ($BaseUrl + "ordens.php?id=$createdOsId") -Token $token
                    $valorTotalEsperado = 610.00
                    $valorTotalOk = ($buscarOs.StatusCode -eq 200 -and [double]$buscarOs.Json.valor_total -eq $valorTotalEsperado)
                    $itensOk = ($buscarOs.StatusCode -eq 200 -and $buscarOs.Json.itens.Count -eq 2)
                    Write-Assert "GET OS lê valor_total calculado" $valorTotalOk (
                        "Status=$($buscarOs.StatusCode); valor_total=$($buscarOs.Json.valor_total); esperado=$valorTotalEsperado"
                    )
                    Write-Assert "GET OS retorna 2 itens" $itensOk (
                        "Status=$($buscarOs.StatusCode); quantidade=$($buscarOs.Json.itens.Count)"
                    )

                    $patchStatus = Invoke-ApiRequest -Method "PATCH" -Url ($BaseUrl + "ordens.php?id=$createdOsId") -Token $token -Body @{
                        status = "EM_ANDAMENTO"
                    }
                    Write-Assert "PATCH status da OS" ($patchStatus.StatusCode -eq 200) (
                        "Status=$($patchStatus.StatusCode); conteúdo=$($patchStatus.Content)"
                    )

                    $putOs = Invoke-ApiRequest -Method "PUT" -Url ($BaseUrl + "ordens.php?id=$createdOsId") -Token $token -Body @{
                        veiculo_id = $createdVehicleId
                        status = "FINALIZADA"
                        observacoes = "OS atualizada pelo teste automatizado $stamp"
                        itens = @(
                            @{ servico_id = 1; quantidade = 1; valor_cobrado = 250.00 },
                            @{ servico_id = 3; quantidade = 1; valor_cobrado = 450.00 }
                        )
                    }
                    Write-Assert "PUT da OS substitui os itens" (
                        $putOs.StatusCode -eq 200
                    ) ("Status=$($putOs.StatusCode); conteúdo=$($putOs.Content)")

                    $deletarOs = Invoke-ApiRequest -Method "DELETE" -Url ($BaseUrl + "ordens.php?id=$createdOsId") -Token $token
                    Write-Assert "DELETE da OS criada" ($deletarOs.StatusCode -eq 200) (
                        "Status=$($deletarOs.StatusCode); conteúdo=$($deletarOs.Content)"
                    )

                    $buscarOsRemovida = Invoke-ApiRequest -Method "GET" -Url ($BaseUrl + "ordens.php?id=$createdOsId") -Token $token
                    Write-Assert "GET da OS removida retorna 404" (
                        $buscarOsRemovida.StatusCode -eq 404
                    ) ("Status=$($buscarOsRemovida.StatusCode); erro=$(Get-JsonError $buscarOsRemovida)")
                    $createdOsId = $null
                }

                $deletarVeiculo = Invoke-ApiRequest -Method "DELETE" -Url ($BaseUrl + "veiculos.php?id=$createdVehicleId") -Token $token
                Write-Assert "Limpeza: excluir veículo único" ($deletarVeiculo.StatusCode -eq 200) (
                    "Status=$($deletarVeiculo.StatusCode); conteúdo=$($deletarVeiculo.Content)"
                )
                $createdVehicleId = $null
            }

            $deletarCliente = Invoke-ApiRequest -Method "DELETE" -Url ($BaseUrl + "clientes.php?id=$createdClientId") -Token $token
            Write-Assert "Limpeza: excluir cliente único" ($deletarCliente.StatusCode -eq 200) (
                "Status=$($deletarCliente.StatusCode); conteúdo=$($deletarCliente.Content)"
            )
            $createdClientId = $null
        }

        $resumo = Invoke-ApiRequest -Method "GET" -Url ($BaseUrl + "resumo.php") -Token $token
        $camposResumo = @(
            "os_abertas",
            "os_em_andamento",
            "os_finalizadas_mes",
            "faturamento_mes",
            "os_finalizadas",
            "os_canceladas"
        )
        $todosCamposResumo = $resumo.StatusCode -eq 200
        if ($todosCamposResumo) {
            foreach ($campo in $camposResumo) {
                if ($null -eq $resumo.Json.PSObject.Properties[$campo]) {
                    $todosCamposResumo = $false
                    break
                }
            }
        }
        Write-Assert "Resumo contém os 6 campos" $todosCamposResumo (
            "Status=$($resumo.StatusCode); conteúdo=$($resumo.Content)"
        )
    }
} catch {
    $script:failed++
    Write-Host ("FAIL - Erro inesperado do script") -ForegroundColor Red
    Write-Host ("       " + $_.Exception.Message) -ForegroundColor Yellow
} finally {
    if ($token) {
        if ($createdOsId) {
            Try-Delete -Method "DELETE" -Url ($BaseUrl + "ordens.php?id=$createdOsId") -Token $token
        }
        if ($createdVehicleId) {
            Try-Delete -Method "DELETE" -Url ($BaseUrl + "veiculos.php?id=$createdVehicleId") -Token $token
        }
        if ($createdClientId) {
            Try-Delete -Method "DELETE" -Url ($BaseUrl + "clientes.php?id=$createdClientId") -Token $token
        }
    }
}

Write-Host "=" * 72
Write-Host ("$passed passaram, $failed falharam") -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })

if ($failed -gt 0) {
    exit 1
}
exit 0
