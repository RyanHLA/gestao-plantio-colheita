package br.edu.ifbaiano.gpc.dto;

import java.time.OffsetDateTime;

/** Registro de uma execução de comando sobre uma safra. */
public record HistoricoComandoDTO(
        OffsetDateTime instante,
        Long safraId,
        String comando,
        boolean sucesso,
        String mensagem) {
}
