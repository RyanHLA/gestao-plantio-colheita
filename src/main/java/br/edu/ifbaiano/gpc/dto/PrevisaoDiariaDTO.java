package br.edu.ifbaiano.gpc.dto;

import java.time.LocalDate;

/**
 * Previsão do tempo de um dia, já convertida para o formato do domínio.
 */
public record PrevisaoDiariaDTO(
        LocalDate data,
        double precipitacaoMm,
        Integer probabilidadeChuvaPercentual,
        double temperaturaMaximaC,
        double temperaturaMinimaC) {
}
