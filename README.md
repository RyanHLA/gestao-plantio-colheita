# Gestão de Plantio e Colheita (GPC)

API REST para pequenos e médios produtores rurais planejarem o plantio e a colheita das suas áreas.

## Problema

O produtor rural decide a data de plantio com base na experiência e em previsões do tempo dispersas. Se ele planta sem chuva suficiente, a semente não germina bem e ele perde dinheiro. Além disso, muitos produtores não têm um registro organizado das suas áreas, das safras e da produção.

A GPC resolve os dois problemas:

1. Registra produtores, talhões (áreas de plantio), culturas e safras.
2. **Sugere a data de plantio** de cada safra com base na previsão de chuva e de temperatura para a localização do talhão.

## Integrantes

| Nome | GitHub |
|---|---|
| Ryan Haiala | [@RyanHLA](https://github.com/RyanHLA) |
| Tarcisio Muniz |[@TarcisioMuniz7](https://github.com/TarcisioMuniz7) |
| Mateus Pereira | [@Mateusjesper](https://github.com/Mateusjesper)|

## API externa: Open-Meteo

[Open-Meteo](https://open-meteo.com/) é uma API pública e gratuita de previsão do tempo. Ela não exige cadastro nem chave de acesso.

- **Endpoint usado:** `GET https://api.open-meteo.com/v1/forecast`
- **Parâmetros enviados:** latitude e longitude do talhão, `daily=precipitation_sum,precipitation_probability_max,temperature_2m_max,temperature_2m_min`, `timezone=America/Sao_Paulo`, `forecast_days=16`
- **Dados usados:** chuva diária (mm), probabilidade de chuva (%), temperatura mínima e máxima (°C), para os próximos 16 dias.

Exemplo de resposta (resumida):

```json
{
  "latitude": -14.25,
  "longitude": -42.75,
  "daily": {
    "time": ["2026-11-06", "2026-11-07", "2026-11-08"],
    "precipitation_sum": [0.1, 0.0, 11.1],
    "precipitation_probability_max": [37, 0, 76],
    "temperature_2m_max": [38.5, 39.3, 37.4],
    "temperature_2m_min": [25.8, 24.8, 23.8]
  }
}
```

### Como a aplicação usa os dados (regra de negócio)

Com a estratégia padrão (`chuva-acumulada`), o `SugestaoPlantioService` procura o **primeiro dia D** da previsão em que:

1. a chuva acumulada de D até D+2 (3 dias) é maior ou igual à **chuva mínima de plantio** da cultura; e
2. a temperatura mínima e a máxima de D estão na faixa adequada da cultura.

A estratégia `chuva-provavel` aplica os mesmos critérios e exige também probabilidade média de chuva de pelo menos 70% nos 3 dias.

A **data prevista de colheita** é a data de plantio mais o **ciclo da cultura** (em dias). Se nenhum dia atende aos critérios, a aplicação informa a maior chuva prevista e recomenda esperar.

## Requisitos Funcionais

| ID | Requisito |
|---|---|
| RF01 | Cadastrar, consultar, alterar e excluir produtores. |
| RF02 | Cadastrar, consultar, alterar e excluir talhões de um produtor, com área, tipo de solo e coordenadas (latitude e longitude). |
| RF03 | Cadastrar, consultar, alterar e excluir culturas, com ciclo em dias, chuva mínima para plantio, faixa de temperatura e produtividade esperada. |
| RF04 | Planejar uma safra, ligando um talhão a uma cultura. |
| RF05 | Consultar a previsão do tempo de 16 dias para um talhão (Open-Meteo). |
| RF06 | Sugerir a data de plantio de uma safra conforme a previsão de chuva e de temperatura. |
| RF07 | Calcular a data prevista de colheita com base no ciclo da cultura. |
| RF08 | Registrar o plantio efetivo de uma safra e recalcular a colheita prevista. |
| RF09 | Registrar a colheita de uma safra, com data e produção em kg. |
| RF10 | Calcular a produção estimada da safra (área do talhão x produtividade esperada). |
| RF11 | Listar as safras por produtor, por talhão e por status (planejada, plantada, colhida, cancelada). |
| RF12 | Impedir duas safras ativas no mesmo talhão ao mesmo tempo. |

## Padrões de projeto aplicados

| Padrão | Tipo | Onde está | Problema que resolve |
|---|---|---|---|
| **Front Controller** | Arquitetural web | `DispatcherServlet` do Spring (ponto único de entrada) + [`frontcontroller/FrontControllerFilter`](src/main/java/br/edu/ifbaiano/gpc/frontcontroller/FrontControllerFilter.java) + [`frontcontroller/TratadorGlobalExcecoes`](src/main/java/br/edu/ifbaiano/gpc/frontcontroller/TratadorGlobalExcecoes.java) | Toda requisição passa primeiro pelo filtro, que gera o `X-Request-Id`, exige a chave `X-API-Key` nas operações de escrita e registra método, rota, status e tempo. Depois o `DispatcherServlet` despacha para o controller. Qualquer exceção volta pelo `TratadorGlobalExcecoes`, que responde um JSON padronizado (`ErroDTO`) com o status HTTP certo. Nenhum controller tem try/catch nem verificação de chave. |
| **DTO** | Arquitetural | Pacote [`dto`](src/main/java/br/edu/ifbaiano/gpc/dto) | Os controllers só recebem `*RequestDTO` e só devolvem `*ResponseDTO`; as entidades JPA nunca saem da API. As validações de entrada (`@NotBlank`, `@Positive`...) ficam no DTO. O `ProdutorResponseDTO` devolve o CPF/CNPJ **mascarado** (`123.***.***-00`). |
| **Repository / DAO** | Arquitetural | Pacote [`repository`](src/main/java/br/edu/ifbaiano/gpc/repository) | Interfaces Spring Data JPA isolam o acesso ao banco. Os services não escrevem SQL nem conhecem o H2. |
| **Command** | GoF comportamental | Pacote [`command`](src/main/java/br/edu/ifbaiano/gpc/command) | Cada operação do ciclo da safra (sugerir plantio, plantar, colher, cancelar) é um objeto `ComandoSafra`. O invocador `ExecutorComandoSafra` carrega a safra, abre a transação, executa o comando e guarda o histórico (`GET /safras/comandos/historico`). Uma operação nova vira só uma classe nova. |
| **Strategy** | GoF comportamental | Pacote [`strategy`](src/main/java/br/edu/ifbaiano/gpc/strategy) | O critério de "dia bom para plantar" é trocável: `ChuvaAcumuladaStrategy` (padrão) e `ChuvaProvavelStrategy` (exige também probabilidade de chuva ≥ 70%). O `SugestaoPlantioService` é o contexto e escolhe a estratégia pelo parâmetro `?estrategia=`, sem if/else. |

Papéis do Command no projeto:

| Papel | Classe |
|---|---|
| Comando (interface) | `ComandoSafra<R>` |
| Comandos concretos | `SugerirPlantioComando`, `PlantarSafraComando`, `ColherSafraComando`, `CancelarSafraComando` |
| Invocador | `ExecutorComandoSafra` |
| Receptor | `Safra` (a entidade contém a regra de cada transição) |
| Cliente | `SafraService` (cria o comando com os parâmetros da requisição) |

## Arquitetura em camadas

```
Postman ──HTTP/JSON──> FrontControllerFilter ──> DispatcherServlet ──> Controller ──> Service ──> Repository ──> H2
                              (Front Controller)                          │    │          │
                                                                        DTOs   │          └─> Comandos (Command)
                                                                               └─> SugestaoPlantioService ─> Estratégias (Strategy)
                                                                                                         └─> OpenMeteoClient ─> Open-Meteo
           TratadorGlobalExcecoes (Front Controller) <── qualquer exceção
```

Os diagramas detalhados estão no diretório [`docs`](docs):

- [Diagrama de Classes](docs/diagrama-classes.md)
- [Diagrama de Componentes](docs/diagrama-componentes.md)
- [Modelo do Banco de Dados](docs/modelo-banco.md)
- [Diagrama de Entidade e Relacionamento (DER)](docs/diagrama-er.md) · [PDF](docs/diagrama-er.pdf)

## Estrutura do código

```
src/main/java/br/edu/ifbaiano/gpc
├── frontcontroller/   Padrão Front Controller: FrontControllerFilter e TratadorGlobalExcecoes
├── controller/        Controllers REST: Produtor, Talhao, Cultura e Safra
├── dto/               Padrão DTO: entrada (*RequestDTO), saída (*ResponseDTO) e ErroDTO
├── service/           Regras de negócio, exceções de negócio e cliente da Open-Meteo
├── command/           Padrão Command: comandos da safra e o ExecutorComandoSafra
├── strategy/          Padrão Strategy: critérios de sugestão de plantio
├── repository/        Padrão Repository/DAO (Spring Data JPA)
└── model/             Entidades JPA: Produtor, Talhao, Cultura, Safra
```

São 8 pastas no mesmo nível, sem subpastas. Cada padrão do enunciado tem uma pasta com o próprio nome.

## Tecnologias

- Java 21;
- Spring Boot 4 (Web MVC, RestClient, Data JPA, Validation);
- H2 (banco em memória);
- Maven (Maven Wrapper incluído);
- JUnit 5;
- Postman (testes da API).

## Como executar

Pré-requisito: JDK 21. O Maven não precisa estar instalado.

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco H2 é em memória: os dados são apagados quando a aplicação para. O console do H2 fica em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:gpc`, usuário `sa`, sem senha).

### Autenticação

O Front Controller exige o cabeçalho **`X-API-Key: gpc-ifbaiano-2026`** em toda requisição `POST`, `PUT`, `PATCH` e `DELETE`. Requisições `GET` são livres. A chave fica em `application.properties` (`gpc.seguranca.api-key`); deixá-la vazia desliga a verificação.

### Testes automatizados

```bash
./mvnw test
```

Rodam os testes de domínio, das estratégias de sugestão e um teste de ponta a ponta da API (`ApiFluxoCompletoTest`, com a Open-Meteo simulada). O teste `OpenMeteoClientTest` faz requisição real à Open-Meteo, precisa de internet e por isso só roda quando pedido:

```bash
./mvnw test -Dgroups=integracao -DexcludedGroups=
```

## Testes no Postman

1. Suba a aplicação (`./mvnw spring-boot:run`).
2. No Postman, **Import** → arquivo [`postman/GPC.postman_collection.json`](postman/GPC.postman_collection.json).
3. Abra a coleção e clique em **Run**. As pastas devem rodar na ordem: cada requisição de cadastro guarda o `id` criado em uma variável da coleção (`produtorId`, `talhaoId`, `safraId`...).

A chave `X-API-Key` já está configurada na aba *Authorization* da coleção. As requisições de sugestão de plantio e de previsão precisam de internet; sem acesso à Open-Meteo a API responde `502 Bad Gateway`.

| Pasta | Conteúdo |
|---|---|
| 1. Produtores | CRUD de produtor (CPF mascarado na resposta) |
| 2. Culturas | Cadastro e alteração de culturas |
| 3. Talhões | CRUD de talhão e previsão de 16 dias |
| 4. Safras (Command + Strategy) | Planejar, trocar cultura, sugerir plantio (duas estratégias), plantar, colher, listar por status, histórico de comandos |
| 5. Erros tratados pelo Front Controller | 401, 400, 404 e 409 com o JSON de erro padronizado |
| 6. Exclusões | Cancelamento e exclusões permitidas (204) |

## Endpoints

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/produtores` | Lista produtores | 200 |
| GET | `/produtores/{id}` | Busca produtor | 200 |
| POST | `/produtores` | Cadastra produtor | 201 |
| PUT | `/produtores/{id}` | Altera produtor | 200 |
| DELETE | `/produtores/{id}` | Exclui produtor sem talhões | 204 |
| GET | `/produtores/{id}/talhoes` | Talhões do produtor | 200 |
| GET | `/produtores/{id}/safras` | Safras do produtor (RF11) | 200 |
| GET | `/talhoes` | Lista talhões | 200 |
| GET | `/talhoes/{id}` | Busca talhão | 200 |
| POST | `/talhoes` | Cadastra talhão | 201 |
| PUT | `/talhoes/{id}` | Altera talhão | 200 |
| DELETE | `/talhoes/{id}` | Exclui talhão sem safras | 204 |
| GET | `/talhoes/{id}/safras` | Safras do talhão (RF11) | 200 |
| GET | `/talhoes/{id}/previsao` | Previsão de 16 dias (RF05) | 200 |
| GET | `/culturas` | Lista culturas | 200 |
| GET | `/culturas/{id}` | Busca cultura | 200 |
| POST | `/culturas` | Cadastra cultura | 201 |
| PUT | `/culturas/{id}` | Altera cultura | 200 |
| DELETE | `/culturas/{id}` | Exclui cultura não usada em safras | 204 |
| GET | `/safras?status=` | Lista safras, com filtro opcional por status (RF11) | 200 |
| GET | `/safras/{id}` | Busca safra | 200 |
| POST | `/safras` | Planeja safra (RF04, RF12) | 201 |
| PUT | `/safras/{id}` | Troca a cultura de uma safra PLANEJADA | 200 |
| DELETE | `/safras/{id}` | Exclui safra PLANEJADA ou CANCELADA | 204 |
| POST | `/safras/{id}/sugestao-plantio?estrategia=` | Comando: sugere a data de plantio (RF06, RF07) | 200 |
| POST | `/safras/{id}/plantio` | Comando: registra o plantio (RF08) | 200 |
| POST | `/safras/{id}/colheita` | Comando: registra a colheita (RF09) | 200 |
| POST | `/safras/{id}/cancelamento` | Comando: cancela a safra | 200 |
| GET | `/safras/comandos/historico` | Histórico dos comandos executados | 200 |
| GET | `/safras/estrategias-sugestao` | Estratégias de sugestão disponíveis | 200 |

Respostas de erro (geradas pelo `TratadorGlobalExcecoes`):

| Status | Quando |
|---|---|
| 400 | JSON inválido, campo obrigatório ausente, valor fora da faixa, status ou estratégia desconhecidos |
| 401 | Escrita sem a chave `X-API-Key` (gerado pelo `FrontControllerFilter`) |
| 404 | Recurso ou rota inexistente |
| 409 | Regra de negócio violada: CPF ou cultura duplicados, duas safras ativas no talhão, transição de status inválida, exclusão bloqueada |
| 502 | Falha ao consultar a Open-Meteo |

Exemplo:

```json
{
  "timestamp": "2026-11-13T19:30:00-03:00",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "O talhão 'Talhão Norte' já tem uma safra planejada ou plantada",
  "caminho": "/safras",
  "campos": null
}
```

## Status

- [x] 1ª entrega (06/11): proposta, modelagem, POJOs e teste da API externa
- [x] Entidades JPA e Repositories
- [x] Controllers REST com DTOs
- [x] Front Controller (filtro + tratamento global de exceções)
- [x] Padrões GoF: Command e Strategy
- [x] Coleção do Postman
- [ ] Entrega final (27/11)
