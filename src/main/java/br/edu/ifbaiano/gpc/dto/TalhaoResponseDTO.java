package br.edu.ifbaiano.gpc.dto;

import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.model.TipoSolo;

/** Dados de saída do talhão. */
public record TalhaoResponseDTO(
        Long id,
        String nome,
        double areaHectares,
        double latitude,
        double longitude,
        TipoSolo tipoSolo,
        Long produtorId,
        String produtorNome,
        boolean ocupado) {

    public static TalhaoResponseDTO de(Talhao t) {
        return new TalhaoResponseDTO(t.getId(), t.getNome(), t.getAreaHectares(),
                t.getLatitude(), t.getLongitude(), t.getTipoSolo(),
                t.getProdutor().getId(), t.getProdutor().getNome(), t.isOcupado());
    }
}
