package br.edu.ifbaiano.gpc.strategy;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Estratégia conservadora: além do critério de chuva acumulada e temperatura,
 * exige que a probabilidade média de chuva na janela seja de pelo menos 70%.
 * Reduz o risco de plantar com base em uma previsão de chuva incerta.
 */
@Component
public class ChuvaProvavelStrategy implements EstrategiaSugestaoPlantio {

    public static final String CODIGO = "chuva-provavel";
    public static final int PROBABILIDADE_MINIMA = 70;

    private final ChuvaAcumuladaStrategy criterioBase = new ChuvaAcumuladaStrategy();

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String descricao() {
        return "Critério da chuva acumulada + probabilidade média de chuva na janela >= "
                + PROBABILIDADE_MINIMA + "%.";
    }

    @Override
    public AvaliacaoDia avaliar(List<PrevisaoDiariaDTO> previsao, int indice, Cultura cultura) {
        AvaliacaoDia base = criterioBase.avaliar(previsao, indice, cultura);
        double probabilidadeMedia = probabilidadeMedia(previsao, indice);
        boolean provavel = probabilidadeMedia >= PROBABILIDADE_MINIMA;

        String justificativa = base.justificativa() + String.format(Locale.ROOT,
                " Probabilidade média de chuva: %.0f%% (mínimo: %d%%).",
                probabilidadeMedia, PROBABILIDADE_MINIMA);
        return new AvaliacaoDia(base.apto() && provavel, base.chuvaJanelaMm(), justificativa);
    }

    private static double probabilidadeMedia(List<PrevisaoDiariaDTO> previsao, int inicio) {
        double soma = 0;
        for (int j = inicio; j < inicio + JANELA_DIAS; j++) {
            Integer p = previsao.get(j).probabilidadeChuvaPercentual();
            soma += p != null ? p : 0;
        }
        return soma / JANELA_DIAS;
    }
}
