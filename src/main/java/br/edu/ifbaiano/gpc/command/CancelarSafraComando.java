package br.edu.ifbaiano.gpc.command;

import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.model.Safra;

/** Cancela uma safra planejada ou plantada, liberando o talhão. */
public class CancelarSafraComando implements ComandoSafra<SafraResponseDTO> {

    @Override
    public String nome() {
        return "CANCELAR";
    }

    @Override
    public SafraResponseDTO executar(Safra safra) {
        safra.cancelar();
        return SafraResponseDTO.de(safra);
    }
}
