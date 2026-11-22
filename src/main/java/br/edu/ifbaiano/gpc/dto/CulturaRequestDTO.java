package br.edu.ifbaiano.gpc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dados de entrada para cadastrar ou alterar uma cultura (POST/PUT). */
public record CulturaRequestDTO(
        @NotBlank @Size(max = 80) String nome,
        @Size(max = 120) String nomeCientifico,
        @NotNull @Positive Integer cicloDias,
        @NotNull @PositiveOrZero Double chuvaMinimaPlantioMm,
        @NotNull Double temperaturaMinimaC,
        @NotNull Double temperaturaMaximaC,
        @NotNull @PositiveOrZero Double produtividadeEsperadaKgHa) {
}
