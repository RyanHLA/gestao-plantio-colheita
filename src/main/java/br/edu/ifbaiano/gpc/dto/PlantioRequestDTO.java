package br.edu.ifbaiano.gpc.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Entrada do comando "registrar plantio". Formato da data: AAAA-MM-DD. */
public record PlantioRequestDTO(@NotNull LocalDate dataPlantio) {
}
