package br.edu.ifbaiano.gpc.dto;

import br.edu.ifbaiano.gpc.model.TipoSolo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados de entrada para cadastrar ou alterar um talhão (POST/PUT). */
public record TalhaoRequestDTO(
        @NotNull Long produtorId,
        @NotBlank @Size(max = 80) String nome,
        @NotNull @Positive Double areaHectares,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        TipoSolo tipoSolo) {
}
