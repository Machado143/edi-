# Graph Report - trabalhoedivaldo  (2026-11-22)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 590 nodes · 1608 edges · 29 communities (12 shown, 17 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 43 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- HomeScreen.kt
- NetworkResult
- OrdemServicoViewModel.kt
- AppContainer.kt
- ApiService
- OrdemServicoViewModel
- SettingsViewModel.kt
- FormValidationTest
- .extractErrorMessage
- ClienteViewModel
- oficina/MainActivity.kt
- SafeDoubleAdapterTest.kt
- Screen
- helpers.php
- HomeScreen
- ExampleInstrumentedTest.kt
- api-tests.ps1

## God Nodes (most connected - your core abstractions)
1. `OrdemServicoViewModel` - 45 edges
2. `ApiService` - 40 edges
3. `NetworkResult` - 32 edges
4. `ClienteViewModel` - 25 edges
5. `apiError()` - 24 edges
6. `VeiculoViewModel` - 22 edges
7. `connectionOrServerMessage()` - 22 edges
8. `DefaultAppContainer` - 21 edges
9. `AppNavigation()` - 21 edges
10. `Cliente` - 19 edges

## Surprising Connections (you probably didn't know these)
- `ClienteRepository` --references--> `ApiService`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/data/repository/ClienteRepository.kt → app/src/main/java/com/exemplo/oficina/data/remote/ApiService.kt
- `AppContainer` --references--> `ClienteRepository`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/di/AppContainer.kt → app/src/main/java/com/exemplo/oficina/data/repository/ClienteRepository.kt
- `DefaultAppContainer` --references--> `ClienteRepository`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/di/AppContainer.kt → app/src/main/java/com/exemplo/oficina/data/repository/ClienteRepository.kt
- `ClienteViewModel` --references--> `ClienteRepository`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/ui/viewmodel/ClienteViewModel.kt → app/src/main/java/com/exemplo/oficina/data/repository/ClienteRepository.kt
- `OrdemServicoViewModel` --references--> `ClienteRepository`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/ui/viewmodel/OrdemServicoViewModel.kt → app/src/main/java/com/exemplo/oficina/data/repository/ClienteRepository.kt

## Import Cycles
- None detected.

## Communities (29 total, 17 thin omitted)

### Community 0 - "HomeScreen.kt"
Cohesion: 0.07
Nodes (5): StatusOS, EmptyStateMessage(), LoadingStateIndicator(), StatSummaryCard(), StatusBadgeComponent()

### Community 1 - "NetworkResult"
Cohesion: 0.07
Nodes (22): ClienteRepository, apiError(), connectionOrServerMessage(), NetworkResult, Error, Loading, Success, OrdemServicoRepository (+14 more)

### Community 2 - "OrdemServicoViewModel.kt"
Cohesion: 0.06
Nodes (15): ServicoRepository, Servico, ServicoCardItem(), ServicoFormDialog(), ServicoScreen(), LoginState, Error, Idle (+7 more)

### Community 3 - "AppContainer.kt"
Cohesion: 0.06
Nodes (12): SessionEventBus, UserPreferencesRepository, PreferencesKeys, AuthRepository, DashboardRepository, AppContainer, DefaultAppContainer, ViewModelFactory (+4 more)

### Community 4 - "ApiService"
Cohesion: 0.11
Nodes (12): ApiService, ClienteDto, LoginRequestDto, LoginResponseDto, MessageResponseDto, OrdemServicoDto, OSServicoItemDto, ResumoDashboardDto (+4 more)

### Community 5 - "OrdemServicoViewModel"
Cohesion: 0.07
Nodes (10): AppNavigation(), LoginScreen(), OrdemServicoDetailScreen(), AddItemDialog(), OrdemServicoFormScreen(), OSFormItemCard(), OrdemServicoCardItem(), OrdemServicoListScreen() (+2 more)

### Community 6 - "SettingsViewModel.kt"
Cohesion: 0.09
Nodes (6): AppSettings, SettingsRepository, Keys, SettingsScreen(), SettingsUiState, SettingsViewModel

### Community 8 - ".extractErrorMessage"
Cohesion: 0.16
Nodes (4): ErrorJsonParser, OsCalculations, ErrorJsonParserTest, OsCalculationsTest

### Community 9 - "ClienteViewModel"
Cohesion: 0.15
Nodes (5): ClienteCardItem(), ClienteFormDialog(), ClienteScreen(), ClienteUiState, ClienteViewModel

### Community 12 - "Screen"
Cohesion: 0.17
Nodes (10): Screen, Clientes, Home, Login, OrdemServicoDetail, OrdemServicoForm, OrdensServico, Servicos (+2 more)

### Community 13 - "helpers.php"
Cohesion: 0.29
Nodes (9): exigirMetodo(), exigirToken(), inteiro(), lerCorpoJson(), obterIdQuery(), obterTokenAuthorization(), responderJson(), textoObrigatorio() (+1 more)

### Community 14 - "HomeScreen"
Cohesion: 0.33
Nodes (4): HomeScreen(), ModuloMenuCard(), OrdemStatusBarChart(), ResumoCard()

## Knowledge Gaps
- **14 isolated node(s):** `Error`, `Loading`, `Success`, `Clientes`, `Home` (+9 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 141 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OrdemServicoViewModel` connect `OrdemServicoViewModel` to `HomeScreen.kt`, `NetworkResult`, `OrdemServicoViewModel.kt`, `AppContainer.kt`, `SettingsViewModel.kt`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Are the 19 inferred relationships involving `apiError()` (e.g. with `.createCliente()` and `.deleteCliente()`) actually correct?**
  _`apiError()` has 19 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Error`, `Loading`, `Success` to the rest of the system?**
  _14 weakly-connected nodes found - possible missing edges._
- **Should `HomeScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06922810661128419 - nodes in this community are weakly interconnected._
- **Why does `ApiService` connect `ApiService` to `NetworkResult`, `OrdemServicoViewModel.kt`, `AppContainer.kt`, `SettingsViewModel.kt`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Should `NetworkResult` be split into smaller, more focused modules?**
  _Cohesion score 0.07330618289522399 - nodes in this community are weakly interconnected._
- **Why does `ClienteViewModel` connect `ClienteViewModel` to `HomeScreen.kt`, `NetworkResult`, `OrdemServicoViewModel.kt`, `AppContainer.kt`, `OrdemServicoViewModel`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._