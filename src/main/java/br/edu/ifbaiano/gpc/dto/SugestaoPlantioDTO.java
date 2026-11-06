package br.edu.ifbaiano.gpc.dto;

import java.time.LocalDate;

/**
 * Resultado da regra de negócio: data sugerida para o plantio e a justificativa.
 * Quando nenhuma data atende aos critérios, dataPlantioSugerida é null.
 */
public record SugestaoPlantioDTO(
        String talhao,
        String cultura,
        LocalDate dataPlantioSugerida,
        LocalDate dataColheitaPrevista,
        double chuvaAcumuladaJanelaMm,
        String justificativa) {

    public boolean encontrouData() {
        return dataPlantioSugerida != null;
    }
}
