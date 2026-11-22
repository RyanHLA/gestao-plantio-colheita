package br.edu.ifbaiano.gpc.strategy;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Estratégia padrão: o dia D é apto quando a chuva acumulada de D até D+2 atinge a
 * chuva mínima da cultura e a temperatura de D está na faixa da cultura.
 */
@Component
public class ChuvaAcumuladaStrategy implements EstrategiaSugestaoPlantio {

    public static final String CODIGO = "chuva-acumulada";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String descricao() {
        return "Chuva acumulada em " + JANELA_DIAS + " dias >= chuva mínima da cultura "
                + "e temperatura do dia dentro da faixa da cultura.";
    }

    @Override
    public AvaliacaoDia avaliar(List<PrevisaoDiariaDTO> previsao, int indice, Cultura cultura) {
        PrevisaoDiariaDTO dia = previsao.get(indice);
        double chuvaJanela = EstrategiaSugestaoPlantio.somarChuva(previsao, indice, JANELA_DIAS);

        boolean chuvaSuficiente = chuvaJanela >= cultura.getChuvaMinimaPlantioMm();
        boolean temperaturaOk = cultura.isTemperaturaAdequada(
                dia.temperaturaMinimaC(), dia.temperaturaMaximaC());

        String justificativa = String.format(Locale.ROOT,
                "Previsão de %.1f mm de chuva entre %s e %s (mínimo da cultura: %.1f mm) "
                        + "e temperatura entre %.1f °C e %.1f °C.",
                chuvaJanela, dia.data(), dia.data().plusDays(JANELA_DIAS - 1),
                cultura.getChuvaMinimaPlantioMm(),
                dia.temperaturaMinimaC(), dia.temperaturaMaximaC());
        return new AvaliacaoDia(chuvaSuficiente && temperaturaOk, chuvaJanela, justificativa);
    }
}
