package br.edu.ifbaiano.gpc.strategy;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.model.Cultura;

import java.util.List;

/**
 * Padrão GoF Strategy: cada implementação é um critério diferente para decidir
 * se um dia da previsão é bom para plantar. O {@code SugestaoPlantioService}
 * escolhe a estratégia em tempo de execução pelo código
 * (parâmetro {@code ?estrategia=} da API), sem if/else no serviço.
 */
public interface EstrategiaSugestaoPlantio {

    /** Quantidade de dias somados para medir a umidade do solo após o plantio. */
    int JANELA_DIAS = 3;

    /** Identificador usado na API (ex.: "chuva-acumulada"). */
    String codigo();

    String descricao();

    /**
     * Avalia o dia {@code indice} da previsão. A janela vai de {@code indice}
     * até {@code indice + JANELA_DIAS - 1}; o chamador garante que ela cabe na lista.
     */
    AvaliacaoDia avaliar(List<PrevisaoDiariaDTO> previsao, int indice, Cultura cultura);

    /** Resultado da avaliação de um dia. */
    record AvaliacaoDia(boolean apto, double chuvaJanelaMm, String justificativa) {
    }

    /** Soma a chuva de {@code dias} dias a partir de {@code inicio}, com 1 casa decimal. */
    static double somarChuva(List<PrevisaoDiariaDTO> previsao, int inicio, int dias) {
        double soma = 0;
        for (int j = inicio; j < inicio + dias; j++) {
            soma += previsao.get(j).precipitacaoMm();
        }
        return Math.round(soma * 10) / 10.0;
    }
}
