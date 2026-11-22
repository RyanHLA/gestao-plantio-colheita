# Diagrama de Componentes

A aplicação usa a arquitetura MVC/REST em camadas. Não há frontend: a resposta JSON é a *View*, testada no Postman.

```mermaid
flowchart TB
    postman["Postman<br/>(cliente HTTP)"]

    subgraph app["Aplicação Spring Boot"]
        direction TB

        subgraph front["Front Controller"]
            fcf["FrontControllerFilter<br/>(X-Request-Id, X-API-Key, log)"]
            ds["DispatcherServlet"]
            tge["TratadorGlobalExcecoes<br/>(@RestControllerAdvice)"]
        end

        subgraph controller["Controller (REST)"]
            pc[ProdutorController]
            tc[TalhaoController]
            cc[CulturaController]
            sc[SafraController]
        end

        subgraph dto["DTO"]
            dtos["ProdutorDTO, TalhaoDTO, CulturaDTO, SafraDTO<br/>PrevisaoDiariaDTO, SugestaoPlantioDTO"]
        end

        subgraph service["Service (regras de negócio)"]
            ps[ProdutorService]
            ts[TalhaoService]
            cs[CulturaService]
            ss[SafraService]
            sps[SugestaoPlantioService]
        end

        subgraph comando["Command"]
            exe["ExecutorComandoSafra<br/>(invocador)"]
            cmds["SugerirPlantio, PlantarSafra,<br/>ColherSafra, CancelarSafra"]
        end

        subgraph strategy["Strategy"]
            est["ChuvaAcumuladaStrategy<br/>ChuvaProvavelStrategy"]
        end

        subgraph integration["Integração"]
            omc[OpenMeteoClient]
        end

        subgraph repository["Repository (DAO)"]
            pr[ProdutorRepository]
            tr[TalhaoRepository]
            cr[CulturaRepository]
            sr[SafraRepository]
        end

        subgraph model["Model (domínio)"]
            ent["Produtor, Talhao, Cultura, Safra"]
        end
    end

    db[("Banco de dados<br/>H2 (em memória)")]
    api(["API Open-Meteo<br/>api.open-meteo.com/v1/forecast"])

    postman -- "HTTP + JSON" --> fcf
    fcf --> ds
    ds --> controller
    controller -. "exceções" .-> tge
    controller -- "recebe e devolve" --> dto
    controller --> service
    ss --> exe
    exe --> cmds
    cmds --> sps
    sps --> est
    sps --> omc
    omc -- "HTTPS GET" --> api
    service --> repository
    service --> model
    repository --> model
    repository -- "JPA" --> db
```

## Responsabilidade de cada camada

| Camada | Pacote | Responsabilidade |
|---|---|---|
| Front Controller | `gpc.frontcontroller` | Ponto de entrada centralizado: rastreio, autenticação por chave, log e tratamento global de exceções. |
| Controller | `gpc.controller` | Recebe as requisições HTTP e devolve JSON. Não tem regra de negócio. |
| DTO | `gpc.dto` | Define os dados de entrada e de saída da API. Isola o modelo de domínio. |
| Service | `gpc.service` | Aplica as regras de negócio. Exemplo: sugestão da data de plantio. |
| Command | `gpc.command` | Encapsula as operações do ciclo da safra; o invocador executa e registra o histórico. |
| Strategy | `gpc.strategy` | Critérios trocáveis para decidir o dia de plantio. |
| Integração | `gpc.service` (OpenMeteoClient) | Chama a API Open-Meteo e converte o JSON para `PrevisaoDiariaDTO`. |
| Repository | `gpc.repository` | Acessa o banco de dados (Spring Data JPA). |
| Model | `gpc.model` | Entidades do domínio e suas regras internas. |

## Fluxo da sugestão de plantio

```mermaid
sequenceDiagram
    actor U as Postman
    participant F as FrontControllerFilter
    participant C as SafraController
    participant SS as SafraService
    participant E as ExecutorComandoSafra
    participant S as SugestaoPlantioService
    participant O as OpenMeteoClient
    participant A as API Open-Meteo

    U->>F: POST /safras/{id}/sugestao-plantio?estrategia=chuva-acumulada
    F->>F: valida X-API-Key, gera X-Request-Id
    F->>C: despacha (via DispatcherServlet)
    C->>SS: sugerirPlantio(id, estrategia)
    SS->>E: executar(id, new SugerirPlantioComando(...))
    E->>S: sugerir(talhao, cultura, estrategia)
    S->>O: buscarPrevisaoDiaria(lat, lon, 16)
    O->>A: GET /v1/forecast?latitude&longitude&daily=...
    A-->>O: JSON com previsão diária
    O-->>S: List<PrevisaoDiariaDTO>
    S->>S: estratégia avalia cada dia até achar o 1º apto
    S-->>E: SugestaoPlantioDTO
    E->>E: grava a sugestão na Safra e registra no histórico
    E-->>C: SugestaoPlantioDTO
    C-->>U: 200 OK (JSON)
```

**Situação atual:** todas as camadas do diagrama estão implementadas.
