package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.integration.openmeteo.OpenMeteoClient;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.model.Talhao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Regra de negócio: sugere a data de plantio conforme a previsão de chuva.
 *
 * <p>Critério: a data sugerida é o primeiro dia D da previsão em que
 * <ol>
 *   <li>a chuva acumulada de D até D + (JANELA_DIAS - 1) é maior ou igual à
 *       chuva mínima de plantio da cultura; e</li>
 *   <li>a temperatura mínima e a máxima previstas em D estão na faixa da cultura.</li>
 * </ol>
 * A data prevista de colheita é a data sugerida mais o ciclo da cultura.
 */
@Service
public class SugestaoPlantioService {

    /** Quantidade de dias somados para medir a umidade do solo após o plantio. */
    public static final int JANELA_DIAS = 3;

    private final OpenMeteoClient openMeteoClient;

    public SugestaoPlantioService(OpenMeteoClient openMeteoClient) {
        this.openMeteoClient = openMeteoClient;
    }

    public SugestaoPlantioDTO sugerir(Talhao talhao, Cultura cultura) {
        List<PrevisaoDiariaDTO> previsao = openMeteoClient.buscarPrevisaoDiaria(
                talhao.getLatitude(), talhao.getLongitude(), OpenMeteoClient.MAX_DIAS_PREVISAO);
        return sugerir(talhao, cultura, previsao);
    }

    /** Aplica a regra sobre uma previsão já obtida. Não acessa a rede (testável isoladamente). */
    public SugestaoPlantioDTO sugerir(Talhao talhao, Cultura cultura, List<PrevisaoDiariaDTO> previsao) {
        for (int i = 0; i + JANELA_DIAS <= previsao.size(); i++) {
            PrevisaoDiariaDTO dia = previsao.get(i);
            double chuvaJanela = somarChuva(previsao, i, JANELA_DIAS);

            boolean chuvaSuficiente = chuvaJanela >= cultura.getChuvaMinimaPlantioMm();
            boolean temperaturaOk = cultura.isTemperaturaAdequada(
                    dia.temperaturaMinimaC(), dia.temperaturaMaximaC());

            if (chuvaSuficiente && temperaturaOk) {
                LocalDate colheita = dia.data().plusDays(cultura.getCicloDias());
                String justificativa = String.format(Locale.ROOT,
                        "Previsão de %.1f mm de chuva entre %s e %s (mínimo da cultura: %.1f mm) "
                                + "e temperatura entre %.1f °C e %.1f °C.",
                        chuvaJanela, dia.data(), dia.data().plusDays(JANELA_DIAS - 1),
                        cultura.getChuvaMinimaPlantioMm(),
                        dia.temperaturaMinimaC(), dia.temperaturaMaximaC());
                return new SugestaoPlantioDTO(talhao.getNome(), cultura.getNome(),
                        dia.data(), colheita, chuvaJanela, justificativa);
            }
        }

        double maiorChuva = maiorChuvaEmJanela(previsao);
        String justificativa = String.format(Locale.ROOT,
                "Nenhum dia dos próximos %d atende à cultura. Maior chuva prevista em %d dias: "
                        + "%.1f mm (mínimo: %.1f mm). Aguarde nova previsão.",
                previsao.size(), JANELA_DIAS, maiorChuva, cultura.getChuvaMinimaPlantioMm());
        return new SugestaoPlantioDTO(talhao.getNome(), cultura.getNome(),
                null, null, maiorChuva, justificativa);
    }

    private static double somarChuva(List<PrevisaoDiariaDTO> previsao, int inicio, int dias) {
        double soma = 0;
        for (int j = inicio; j < inicio + dias; j++) {
            soma += previsao.get(j).precipitacaoMm();
        }
        return Math.round(soma * 10) / 10.0;
    }

    private static double maiorChuvaEmJanela(List<PrevisaoDiariaDTO> previsao) {
        double maior = 0;
        for (int i = 0; i + JANELA_DIAS <= previsao.size(); i++) {
            maior = Math.max(maior, somarChuva(previsao, i, JANELA_DIAS));
        }
        return maior;
    }
}
