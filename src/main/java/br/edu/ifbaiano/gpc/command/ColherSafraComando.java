package br.edu.ifbaiano.gpc.command;

import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.model.Safra;

import java.time.LocalDate;

/** Registra a colheita, com data e produção em kg (RF09). */
public class ColherSafraComando implements ComandoSafra<SafraResponseDTO> {

    private final LocalDate dataColheita;
    private final double producaoKg;

    public ColherSafraComando(LocalDate dataColheita, double producaoKg) {
        this.dataColheita = dataColheita;
        this.producaoKg = producaoKg;
    }

    @Override
    public String nome() {
        return "COLHER";
    }

    @Override
    public SafraResponseDTO executar(Safra safra) {
        safra.colher(dataColheita, producaoKg);
        return SafraResponseDTO.de(safra);
    }
}
