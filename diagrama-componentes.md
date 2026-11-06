# Diagrama de Componentes

A aplicação usa a arquitetura MVC/REST em camadas. Não há frontend: a resposta JSON é a *View*, testada no Postman.

```mermaid
flowchart TB
    postman["Postman<br/>(cliente HTTP)"]

    subgraph app["Aplicação Spring Boot"]
        direction TB

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

    db[("Banco de dados<br/>H2 / PostgreSQL")]
    api(["API Open-Meteo<br/>api.open-meteo.com/v1/forecast"])

    postman -- "HTTP + JSON" --> controller
    controller -- "recebe e devolve" --> dto
    controller --> service
    ss --> sps
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
| Controller | `gpc.controller` | Recebe as requisições HTTP e devolve JSON. Não tem regra de negócio. |
| DTO | `gpc.dto` | Define os dados de entrada e de saída da API. Isola o modelo de domínio. |
| Service | `gpc.service` | Aplica as regras de negócio. Exemplo: sugestão da data de plantio. |
| Integração | `gpc.integration.openmeteo` | Chama a API Open-Meteo e converte o JSON para `PrevisaoDiariaDTO`. |
| Repository | `gpc.repository` | Acessa o banco de dados (Spring Data JPA). |
| Model | `gpc.model` | Entidades do domínio e suas regras internas. |

## Fluxo da sugestão de plantio

```mermaid
sequenceDiagram
    actor U as Postman
    participant C as SafraController
    participant S as SugestaoPlantioService
    participant O as OpenMeteoClient
    participant A as API Open-Meteo

    U->>C: POST /safras/{id}/sugestao-plantio
    C->>S: sugerir(talhao, cultura)
    S->>O: buscarPrevisaoDiaria(lat, lon, 16)
    O->>A: GET /v1/forecast?latitude&longitude&daily=...
    A-->>O: JSON com previsão diária
    O-->>S: List<PrevisaoDiariaDTO>
    S->>S: procura o 1º dia com chuva e temperatura adequadas
    S-->>C: SugestaoPlantioDTO
    C-->>U: 200 OK (JSON)
```

**Situação na 1ª entrega:** o Model, os DTOs, o `OpenMeteoClient` e o `SugestaoPlantioService` estão implementados. Os Controllers e os Repositories são o próximo passo.
