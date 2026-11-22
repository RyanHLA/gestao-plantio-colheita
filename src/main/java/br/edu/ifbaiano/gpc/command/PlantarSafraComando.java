package br.edu.ifbaiano.gpc.command;

import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.model.Safra;

import java.time.LocalDate;

/** Registra o plantio efetivo e recalcula a colheita prevista (RF08). */
public class PlantarSafraComando implements ComandoSafra<SafraResponseDTO> {

    private final LocalDate dataPlantio;

    public PlantarSafraComando(LocalDate dataPlantio) {
        this.dataPlantio = dataPlantio;
    }

    @Override
    public String nome() {
        return "PLANTAR";
    }

    @Override
    public SafraResponseDTO executar(Safra safra) {
        safra.plantar(dataPlantio);
        return SafraResponseDTO.de(safra);
    }
}
