package br.edu.ifbaiano.gpc.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para planejar uma safra (POST) ou trocar a cultura de uma
 * safra ainda planejada (PUT). No PUT o talhão não muda.
 */
public record SafraRequestDTO(
        @NotNull Long talhaoId,
        @NotNull Long culturaId) {
}
