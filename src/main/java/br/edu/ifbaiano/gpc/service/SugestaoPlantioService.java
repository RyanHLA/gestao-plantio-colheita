package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.EstrategiaSugestaoDTO;
import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.strategy.ChuvaAcumuladaStrategy;
import br.edu.ifbaiano.gpc.strategy.EstrategiaSugestaoPlantio;
import br.edu.ifbaiano.gpc.strategy.EstrategiaSugestaoPlantio.AvaliacaoDia;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static br.edu.ifbaiano.gpc.strategy.EstrategiaSugestaoPlantio.JANELA_DIAS;

/**
 * Regra de negócio: sugere a data de plantio conforme a previsão do tempo.
 *
 * <p>É o <b>contexto</b> do padrão Strategy: percorre os dias da previsão e
 * delega a decisão "este dia é bom para plantar?" à estratégia escolhida.
 * A data sugerida é o primeiro dia aprovado; a colheita prevista é essa data
 * mais o ciclo da cultura.
 */
@Service
public class SugestaoPlantioService {

    public static final String ESTRATEGIA_PADRAO = ChuvaAcumuladaStrategy.CODIGO;

    private final OpenMeteoClient openMeteoClient;
    private final Map<String, EstrategiaSugestaoPlantio> estrategias = new LinkedHashMap<>();

    public SugestaoPlantioService(OpenMeteoClient openMeteoClient,
                                  List<EstrategiaSugestaoPlantio> estrategias) {
        this.openMeteoClient = openMeteoClient;
        estrategias.forEach(e -> this.estrategias.put(e.codigo(), e));
        if (!this.estrategias.containsKey(ESTRATEGIA_PADRAO)) {
            throw new IllegalStateException("Estratégia padrão não registrada: " + ESTRATEGIA_PADRAO);
        }
    }

    /** Previsão diária (16 dias) para a localização do talhão. */
    public List<PrevisaoDiariaDTO> buscarPrevisao(Talhao talhao) {
        return openMeteoClient.buscarPrevisaoDiaria(
                talhao.getLatitude(), talhao.getLongitude(), OpenMeteoClient.MAX_DIAS_PREVISAO);
    }

    /** Consulta a Open-Meteo e aplica a estratégia escolhida (null = padrão). */
    public SugestaoPlantioDTO sugerir(Talhao talhao, Cultura cultura, String codigoEstrategia) {
        EstrategiaSugestaoPlantio estrategia = estrategia(codigoEstrategia);
        return sugerir(talhao, cultura, buscarPrevisao(talhao), estrategia);
    }

    /** Aplica a estratégia padrão sobre uma previsão já obtida. Não acessa a rede. */
    public SugestaoPlantioDTO sugerir(Talhao talhao, Cultura cultura, List<PrevisaoDiariaDTO> previsao) {
        return sugerir(talhao, cultura, previsao, estrategia(null));
    }

    /** Aplica uma estratégia sobre uma previsão já obtida. Não acessa a rede (testável isoladamente). */
    public SugestaoPlantioDTO sugerir(Talhao talhao, Cultura cultura,
                                      List<PrevisaoDiariaDTO> previsao,
                                      EstrategiaSugestaoPlantio estrategia) {
        int diasAvaliados = Math.max(0, previsao.size() - JANELA_DIAS + 1);
        double maiorChuva = 0;

        for (int i = 0; i < diasAvaliados; i++) {
            AvaliacaoDia avaliacao = estrategia.avaliar(previsao, i, cultura);
            maiorChuva = Math.max(maiorChuva, avaliacao.chuvaJanelaMm());

            if (avaliacao.apto()) {
                LocalDate plantio = previsao.get(i).data();
                return new SugestaoPlantioDTO(talhao.getNome(), cultura.getNome(), estrategia.codigo(),
                        plantio, plantio.plusDays(cultura.getCicloDias()),
                        avaliacao.chuvaJanelaMm(), avaliacao.justificativa());
            }
        }

        String justificativa = String.format(Locale.ROOT,
                "Nenhum dia dos próximos %d atende à cultura pelo critério '%s'. "
                        + "Maior chuva prevista em %d dias: %.1f mm (mínimo: %.1f mm). "
                        + "Aguarde nova previsão.",
                diasAvaliados, estrategia.codigo(), JANELA_DIAS, maiorChuva,
                cultura.getChuvaMinimaPlantioMm());
        return new SugestaoPlantioDTO(talhao.getNome(), cultura.getNome(), estrategia.codigo(),
                null, null, maiorChuva, justificativa);
    }

    /** Busca a estratégia pelo código. Código vazio ou nulo devolve a padrão. */
    public EstrategiaSugestaoPlantio estrategia(String codigo) {
        String chave = (codigo == null || codigo.isBlank()) ? ESTRATEGIA_PADRAO : codigo.trim();
        EstrategiaSugestaoPlantio estrategia = estrategias.get(chave);
        if (estrategia == null) {
            throw new IllegalArgumentException("Estratégia desconhecida: '" + codigo
                    + "'. Use uma destas: " + estrategias.keySet());
        }
        return estrategia;
    }

    public List<EstrategiaSugestaoDTO> listarEstrategias() {
        return estrategias.values().stream()
                .map(e -> new EstrategiaSugestaoDTO(e.codigo(), e.descricao(),
                        e.codigo().equals(ESTRATEGIA_PADRAO)))
                .toList();
    }
}
