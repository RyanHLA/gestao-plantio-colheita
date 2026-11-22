package br.edu.ifbaiano.gpc.dto;

import br.edu.ifbaiano.gpc.model.Safra;
import br.edu.ifbaiano.gpc.model.StatusSafra;

import java.time.LocalDate;

/** Dados de saída da safra. */
public record SafraResponseDTO(
        Long id,
        Long talhaoId,
        String talhaoNome,
        Long culturaId,
        String culturaNome,
        StatusSafra status,
        LocalDate dataPlantioSugerida,
        LocalDate dataPlantio,
        LocalDate dataColheitaPrevista,
        LocalDate dataColheita,
        double producaoEstimadaKg,
        Double producaoKg) {

    public static SafraResponseDTO de(Safra s) {
        return new SafraResponseDTO(s.getId(),
                s.getTalhao().getId(), s.getTalhao().getNome(),
                s.getCultura().getId(), s.getCultura().getNome(),
                s.getStatus(), s.getDataPlantioSugerida(), s.getDataPlantio(),
                s.getDataColheitaPrevista(), s.getDataColheita(),
                s.getProducaoEstimadaKg(), s.getProducaoKg());
    }
}
