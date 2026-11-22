package br.edu.ifbaiano.gpc.dto;

import br.edu.ifbaiano.gpc.model.Cultura;

/** Dados de saída da cultura. */
public record CulturaResponseDTO(
        Long id,
        String nome,
        String nomeCientifico,
        int cicloDias,
        double chuvaMinimaPlantioMm,
        double temperaturaMinimaC,
        double temperaturaMaximaC,
        double produtividadeEsperadaKgHa) {

    public static CulturaResponseDTO de(Cultura c) {
        return new CulturaResponseDTO(c.getId(), c.getNome(), c.getNomeCientifico(),
                c.getCicloDias(), c.getChuvaMinimaPlantioMm(), c.getTemperaturaMinimaC(),
                c.getTemperaturaMaximaC(), c.getProdutividadeEsperadaKgHa());
    }
}
