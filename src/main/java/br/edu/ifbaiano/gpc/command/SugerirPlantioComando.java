package br.edu.ifbaiano.gpc.command;

import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.Safra;
import br.edu.ifbaiano.gpc.model.StatusSafra;
import br.edu.ifbaiano.gpc.service.SugestaoPlantioService;

/**
 * Consulta a previsão do tempo, calcula a data de plantio com a estratégia
 * escolhida e grava a sugestão na safra (RF06 e RF07).
 */
public class SugerirPlantioComando implements ComandoSafra<SugestaoPlantioDTO> {

    private final SugestaoPlantioService sugestaoService;
    private final String estrategia;

    public SugerirPlantioComando(SugestaoPlantioService sugestaoService, String estrategia) {
        this.sugestaoService = sugestaoService;
        this.estrategia = estrategia;
    }

    @Override
    public String nome() {
        return "SUGERIR_PLANTIO";
    }

    @Override
    public SugestaoPlantioDTO executar(Safra safra) {
        // valida o status antes de chamar a API externa
        if (safra.getStatus() != StatusSafra.PLANEJADA) {
            throw new IllegalStateException(
                    "Operação exige status PLANEJADA, mas a safra está " + safra.getStatus());
        }
        SugestaoPlantioDTO sugestao = sugestaoService.sugerir(safra.getTalhao(), safra.getCultura(), estrategia);
        safra.registrarSugestaoPlantio(sugestao.dataPlantioSugerida());
        return sugestao;
    }
}
