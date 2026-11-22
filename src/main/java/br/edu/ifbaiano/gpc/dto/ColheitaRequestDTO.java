package br.edu.ifbaiano.gpc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/** Entrada do comando "registrar colheita". */
public record ColheitaRequestDTO(
        @NotNull LocalDate dataColheita,
        @NotNull @PositiveOrZero Double producaoKg) {
}
