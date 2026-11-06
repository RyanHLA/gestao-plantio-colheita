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
| Tarcisio Muniz | |
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

O `SugestaoPlantioService` procura o **primeiro dia D** da previsão em que:

1. a chuva acumulada de D até D+2 (3 dias) é maior ou igual à **chuva mínima de plantio** da cultura; e
2. a temperatura mínima e a máxima de D estão na faixa adequada da cultura.

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

## Modelagem

Os diagramas estão no diretório [`/docs`](docs):

- [Diagrama de Classes](docs/diagrama-classes.md)
- [Diagrama de Componentes](docs/diagrama-componentes.md)
- [Modelo do Banco de Dados (DER)](docs/modelo-banco.md)

## Tecnologias

- Java 21;
- Spring Boot 4 (Web MVC, RestClient, Data JPA, Validation);
- H2 (banco em memória, desenvolvimento);
- Maven (Maven Wrapper incluído);
- Postman (testes da API).

## Estrutura do código

```
src/main/java/br/edu/ifbaiano/gpc
├── model/                    POJOs do domínio: Produtor, Talhao, Cultura, Safra
├── dto/                      PrevisaoDiariaDTO, SugestaoPlantioDTO
├── integration/openmeteo/    Cliente da API Open-Meteo
└── service/                  SugestaoPlantioService (regra de negócio)
```

## Como executar

Pré-requisito: JDK 21. O Maven não precisa estar instalado.

```bash
# Windows
mvnw.cmd test

# Linux / macOS
./mvnw test
```

O teste `OpenMeteoClientTest` faz uma requisição real à Open-Meteo e mostra a previsão no console. Ele precisa de acesso à internet.

## Status

- [x] 1ª entrega (06/11): proposta, modelagem, POJOs e teste da API externa
- [ ] Repositories e Controllers REST
- [ ] Coleção do Postman
- [ ] Entrega final (27/11)
