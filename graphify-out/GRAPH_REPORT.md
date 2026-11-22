# Graph Report - trabalhoedivaldo  (2026-11-22)

## Corpus Check
- 62 files · ~22,697 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 19 file(s) not represented in the graph (top: .xml 10, .properties 3, (none) 2)

## Summary
- 592 nodes · 1608 edges · 38 communities (15 shown, 23 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 43 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `52ae673e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HomeScreen.kt
- NetworkResult
- ServicoViewModel
- DefaultAppContainer
- ApiService
- OrdemServicoViewModel
- SettingsViewModel.kt
- FormValidationTest
- .extractErrorMessage
- ClienteViewModel
- oficina/MainActivity.kt
- LoginViewModel
- Screen
- helpers.php
- AppNavigation.kt
- ExampleInstrumentedTest.kt
- api-tests.ps1
- UserPreferencesRepository
- DashboardViewModel.kt
- SessionEventBus.kt
- .create

## God Nodes (most connected - your core abstractions)
1. `OrdemServicoViewModel` - 45 edges
2. `ApiService` - 40 edges
3. `NetworkResult` - 32 edges
4. `ClienteViewModel` - 25 edges
5. `apiError()` - 24 edges
6. `VeiculoViewModel` - 22 edges
7. `connectionOrServerMessage()` - 22 edges
8. `AppNavigation()` - 21 edges
9. `DefaultAppContainer` - 21 edges
10. `Cliente` - 19 edges

## Surprising Connections (you probably didn't know these)
- `SafeDoubleAdapterTest` --calls--> `SafeDoubleAdapter`  [INFERRED]
  app/src/test/java/com/exemplo/oficina/data/remote/SafeDoubleAdapterTest.kt → app/src/main/java/com/exemplo/oficina/data/remote/Dtos.kt
- `AppNavigation()` --references--> `AppContainer`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/ui/navigation/AppNavigation.kt → app/src/main/java/com/exemplo/oficina/di/AppContainer.kt
- `AppNavigation()` --calls--> `ViewModelFactory`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/ui/navigation/AppNavigation.kt → app/src/main/java/com/exemplo/oficina/di/AppContainer.kt
- `AppNavigation()` --calls--> `ClienteScreen()`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/ui/navigation/AppNavigation.kt → app/src/main/java/com/exemplo/oficina/ui/screens/ClienteScreen.kt
- `AppNavigation()` --calls--> `OrdemServicoDetailScreen()`  [EXTRACTED]
  app/src/main/java/com/exemplo/oficina/ui/navigation/AppNavigation.kt → app/src/main/java/com/exemplo/oficina/ui/screens/OrdemServicoDetailScreen.kt

## Import Cycles
- None detected.

## Communities (38 total, 23 thin omitted)

### Community 0 - "HomeScreen.kt"
Cohesion: 0.07
Nodes (4): EmptyStateMessage(), LoadingStateIndicator(), StatSummaryCard(), StatusBadgeComponent()

### Community 1 - "NetworkResult"
Cohesion: 0.08
Nodes (21): ClienteRepository, apiError(), connectionOrServerMessage(), NetworkResult, Error, Loading, Success, OrdemServicoRepository (+13 more)

### Community 2 - "ServicoViewModel"
Cohesion: 0.14
Nodes (7): ServicoRepository, Servico, ServicoCardItem(), ServicoFormDialog(), ServicoScreen(), ServicoUiState, ServicoViewModel

### Community 3 - "DefaultAppContainer"
Cohesion: 0.23
Nodes (3): AppContainer, DefaultAppContainer, OficinaApplication

### Community 4 - "ApiService"
Cohesion: 0.11
Nodes (13): ApiService, ClienteDto, LoginRequestDto, LoginResponseDto, MessageResponseDto, OrdemServicoDto, OSServicoItemDto, ResumoDashboardDto (+5 more)

### Community 5 - "OrdemServicoViewModel"
Cohesion: 0.08
Nodes (10): OrdemServicoDetailScreen(), AddItemDialog(), OrdemServicoFormScreen(), OSFormItemCard(), OrdemServicoCardItem(), OrdemServicoListScreen(), StatusBadgeChip(), OrdemServicoFormUiState (+2 more)

### Community 6 - "SettingsViewModel.kt"
Cohesion: 0.09
Nodes (6): AppSettings, SettingsRepository, Keys, SettingsScreen(), SettingsUiState, SettingsViewModel

### Community 8 - ".extractErrorMessage"
Cohesion: 0.09
Nodes (5): ErrorJsonParser, OsCalculations, ErrorJsonParserTest, SafeDoubleAdapterTest, OsCalculationsTest

### Community 9 - "ClienteViewModel"
Cohesion: 0.11
Nodes (6): ClienteCardItem(), ClienteFormDialog(), ClienteScreen(), InputMasks, ClienteUiState, ClienteViewModel

### Community 11 - "LoginViewModel"
Cohesion: 0.17
Nodes (7): LoginState, Error, Idle, Loading, Success, LoginUiState, LoginViewModel

### Community 12 - "Screen"
Cohesion: 0.17
Nodes (10): Screen, Clientes, Home, Login, OrdemServicoDetail, OrdemServicoForm, OrdensServico, Servicos (+2 more)

### Community 13 - "helpers.php"
Cohesion: 0.29
Nodes (9): exigirMetodo(), exigirToken(), inteiro(), lerCorpoJson(), obterIdQuery(), obterTokenAuthorization(), responderJson(), textoObrigatorio() (+1 more)

### Community 14 - "AppNavigation.kt"
Cohesion: 0.13
Nodes (6): AppNavigation(), HomeScreen(), ModuloMenuCard(), OrdemStatusBarChart(), ResumoCard(), LoginScreen()

### Community 29 - "UserPreferencesRepository"
Cohesion: 0.22
Nodes (3): UserPreferencesRepository, PreferencesKeys, AuthRepository

### Community 30 - "DashboardViewModel.kt"
Cohesion: 0.33
Nodes (4): DashboardRepository, ResumoDashboard, DashboardUiState, DashboardViewModel

## Knowledge Gaps
- **14 isolated node(s):** `Error`, `Loading`, `Success`, `Clientes`, `Home` (+9 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 144 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OrdemServicoViewModel` connect `OrdemServicoViewModel` to `HomeScreen.kt`, `.create`, `NetworkResult`, `ServicoViewModel`, `SettingsViewModel.kt`, `FormValidationTest`, `AppNavigation.kt`, `AppContainer.kt`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Are the 19 inferred relationships involving `apiError()` (e.g. with `.createCliente()` and `.deleteCliente()`) actually correct?**
  _`apiError()` has 19 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Error`, `Loading`, `Success` to the rest of the system?**
  _14 weakly-connected nodes found - possible missing edges._
- **Should `HomeScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06880733944954129 - nodes in this community are weakly interconnected._
- **Why does `ApiService` connect `ApiService` to `NetworkResult`, `ServicoViewModel`, `DefaultAppContainer`, `SettingsViewModel.kt`, `UserPreferencesRepository`, `DashboardViewModel.kt`, `AppContainer.kt`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Should `NetworkResult` be split into smaller, more focused modules?**
  _Cohesion score 0.07757885763000852 - nodes in this community are weakly interconnected._
- **Why does `ClienteViewModel` connect `ClienteViewModel` to `HomeScreen.kt`, `.create`, `NetworkResult`, `AppNavigation.kt`, `AppContainer.kt`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._