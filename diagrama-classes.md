# Diagrama de Classes

Classes do modelo de domínio (`br.edu.ifbaiano.gpc.model`).

```mermaid
classDiagram
    direction LR

    class Produtor {
        -Long id
        -String nome
        -String cpfCnpj
        -String email
        -String telefone
        -String municipio
        -String uf
        -List~Talhao~ talhoes
        +adicionarTalhao(Talhao)
        +removerTalhao(Talhao)
        +getAreaTotalHectares() double
    }

    class Talhao {
        -Long id
        -String nome
        -double areaHectares
        -double latitude
        -double longitude
        -TipoSolo tipoSolo
        -Produtor produtor
        -List~Safra~ safras
        +adicionarSafra(Safra)
        +isOcupado() boolean
    }

    class Cultura {
        -Long id
        -String nome
        -String nomeCientifico
        -int cicloDias
        -double chuvaMinimaPlantioMm
        -double temperaturaMinimaC
        -double temperaturaMaximaC
        -double produtividadeEsperadaKgHa
        +isTemperaturaAdequada(double, double) boolean
    }

    class Safra {
        -Long id
        -Talhao talhao
        -Cultura cultura
        -LocalDate dataPlantioSugerida
        -LocalDate dataPlantio
        -LocalDate dataColheitaPrevista
        -LocalDate dataColheita
        -Double producaoKg
        -StatusSafra status
        +registrarSugestaoPlantio(LocalDate)
        +plantar(LocalDate)
        +colher(LocalDate, double)
        +cancelar()
        +getProducaoEstimadaKg() double
        +isAtiva() boolean
    }

    class StatusSafra {
        <<enumeration>>
        PLANEJADA
        PLANTADA
        COLHIDA
        CANCELADA
    }

    class TipoSolo {
        <<enumeration>>
        ARENOSO
        ARGILOSO
        SILTOSO
        HUMIFERO
        MISTO
    }

    Produtor "1" --> "0..*" Talhao : possui
    Talhao "1" --> "0..*" Safra : recebe
    Cultura "1" <-- "0..*" Safra : cultiva
    Safra --> StatusSafra
    Talhao --> TipoSolo
```

## Relacionamentos

| Relacionamento | Cardinalidade | Descrição |
|---|---|---|
| Produtor - Talhão | 1 : N | Um produtor tem vários talhões. Cada talhão pertence a um produtor. |
| Talhão - Safra | 1 : N | Um talhão tem um histórico de safras. |
| Cultura - Safra | 1 : N | Uma cultura pode ser plantada em várias safras. |

A `Safra` liga o `Talhão` à `Cultura`. Ela guarda as datas do ciclo: plantio sugerido, plantio real, colheita prevista e colheita real.

## Ciclo de vida da Safra

```mermaid
stateDiagram-v2
    [*] --> PLANEJADA
    PLANEJADA --> PLANTADA : plantar()
    PLANTADA --> COLHIDA : colher()
    PLANEJADA --> CANCELADA : cancelar()
    PLANTADA --> CANCELADA : cancelar()
    COLHIDA --> [*]
    CANCELADA --> [*]
```
