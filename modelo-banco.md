# Modelo do Banco de Dados (DER)

```mermaid
erDiagram
    PRODUTOR ||--o{ TALHAO : possui
    TALHAO ||--o{ SAFRA : recebe
    CULTURA ||--o{ SAFRA : "é cultivada em"

    PRODUTOR {
        BIGINT id PK
        VARCHAR(120) nome "NOT NULL"
        VARCHAR(18) cpf_cnpj "UNIQUE, NOT NULL"
        VARCHAR(120) email
        VARCHAR(20) telefone
        VARCHAR(80) municipio
        CHAR(2) uf
    }

    TALHAO {
        BIGINT id PK
        BIGINT produtor_id FK "NOT NULL"
        VARCHAR(80) nome "NOT NULL"
        DECIMAL area_hectares "NOT NULL, > 0"
        DECIMAL latitude "NOT NULL, -90 a 90"
        DECIMAL longitude "NOT NULL, -180 a 180"
        VARCHAR(20) tipo_solo
    }

    CULTURA {
        BIGINT id PK
        VARCHAR(80) nome "UNIQUE, NOT NULL"
        VARCHAR(120) nome_cientifico
        INT ciclo_dias "NOT NULL, > 0"
        DECIMAL chuva_minima_plantio_mm "NOT NULL"
        DECIMAL temperatura_minima_c
        DECIMAL temperatura_maxima_c
        DECIMAL produtividade_esperada_kg_ha
    }

    SAFRA {
        BIGINT id PK
        BIGINT talhao_id FK "NOT NULL"
        BIGINT cultura_id FK "NOT NULL"
        DATE data_plantio_sugerida
        DATE data_plantio
        DATE data_colheita_prevista
        DATE data_colheita
        DECIMAL producao_kg
        VARCHAR(20) status "NOT NULL"
    }
```

## Regras de integridade

- `SAFRA.status` aceita só: `PLANEJADA`, `PLANTADA`, `COLHIDA`, `CANCELADA`.
- `SAFRA.data_colheita` deve ser igual ou posterior a `SAFRA.data_plantio`.
- Um talhão não pode ter duas safras ativas (`PLANEJADA` ou `PLANTADA`) ao mesmo tempo. O `SafraService` valida esta regra.
- A exclusão de um produtor com talhões é bloqueada (sem `ON DELETE CASCADE`).
