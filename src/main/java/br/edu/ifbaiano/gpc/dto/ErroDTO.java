package br.edu.ifbaiano.gpc.dto;

import java.time.OffsetDateTime;
import java.util.Map;

/** Corpo padronizado de todas as respostas de erro da API. */
public record ErroDTO(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos) {
}
