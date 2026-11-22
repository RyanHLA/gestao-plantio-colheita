package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.model.TipoSolo;
import br.edu.ifbaiano.gpc.strategy.ChuvaAcumuladaStrategy;
import br.edu.ifbaiano.gpc.strategy.ChuvaProvavelStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa a regra de negócio com previsões fixas (sem acesso à rede). */
class SugestaoPlantioServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 6);

    private final SugestaoPlantioService service = new SugestaoPlantioService(null,
            List.of(new ChuvaAcumuladaStrategy(), new ChuvaProvavelStrategy()));
    private final Talhao talhao = new Talhao(1L, "Talhão Norte", 10, -14.22, -42.78, TipoSolo.ARGILOSO);
    // Feijão: ciclo de 90 dias, precisa de 15 mm em 3 dias, temperatura entre 12 e 32 °C
    private final Cultura feijao = new Cultura(2L, "Feijão", "Phaseolus vulgaris", 90, 15.0, 12.0, 32.0, 1800);

    @Test
    void deveSugerirPrimeiroDiaComChuvaAcumuladaSuficiente() {
        // chuva por dia: 0, 2, 4, 6, 8, 10, 0 -> janelas: 6, 12, 18, 24, ...
        List<PrevisaoDiariaDTO> previsao = previsao(new double[]{0, 2, 4, 6, 8, 10, 0}, 18, 30);

        SugestaoPlantioDTO sugestao = service.sugerir(talhao, feijao, previsao);

        assertEquals(HOJE.plusDays(2), sugestao.dataPlantioSugerida());
        assertEquals(HOJE.plusDays(2 + 90), sugestao.dataColheitaPrevista());
        assertEquals(18.0, sugestao.chuvaAcumuladaJanelaMm(), 0.001);
    }

    @Test
    void naoDeveSugerirQuandoChuvaInsuficiente() {
        List<PrevisaoDiariaDTO> previsao = previsao(new double[]{1, 1, 1, 1, 1, 1, 1}, 18, 30);

        SugestaoPlantioDTO sugestao = service.sugerir(talhao, feijao, previsao);

        assertFalse(sugestao.encontrouData());
        assertNull(sugestao.dataColheitaPrevista());
        assertEquals(3.0, sugestao.chuvaAcumuladaJanelaMm(), 0.001);
    }

    @Test
    void naoDeveSugerirQuandoTemperaturaForaDaFaixa() {
        // chuva suficiente, mas máxima de 36 °C é acima do limite do feijão (32 °C)
        List<PrevisaoDiariaDTO> previsao = previsao(new double[]{10, 10, 10, 10}, 20, 36);

        assertFalse(service.sugerir(talhao, feijao, previsao).encontrouData());
    }

    @Test
    void estrategiaChuvaProvavelDeveExigirProbabilidadeAlta() {
        // chuva suficiente desde o 1º dia, mas a probabilidade média só chega a 70% na janela do dia 3
        // (30 + 90 + 90) / 3 = 70
        List<PrevisaoDiariaDTO> previsao = previsao(new double[]{10, 10, 10, 10, 10, 10, 10, 10}, 18, 30,
                new int[]{30, 30, 30, 30, 90, 90, 90, 90});

        SugestaoPlantioDTO padrao = service.sugerir(talhao, feijao, previsao);
        SugestaoPlantioDTO conservadora = service.sugerir(talhao, feijao, previsao,
                service.estrategia(ChuvaProvavelStrategy.CODIGO));

        assertEquals(HOJE, padrao.dataPlantioSugerida());
        assertEquals(ChuvaAcumuladaStrategy.CODIGO, padrao.estrategia());
        assertEquals(HOJE.plusDays(3), conservadora.dataPlantioSugerida());
        assertEquals(ChuvaProvavelStrategy.CODIGO, conservadora.estrategia());
    }

    @Test
    void deveRejeitarEstrategiaDesconhecida() {
        assertThrows(IllegalArgumentException.class, () -> service.estrategia("adivinhacao"));
        assertEquals(2, service.listarEstrategias().size());
        assertTrue(service.listarEstrategias().getFirst().padrao());
    }

    private static List<PrevisaoDiariaDTO> previsao(double[] chuvaPorDia, double tMin, double tMax) {
        int[] probabilidades = new int[chuvaPorDia.length];
        java.util.Arrays.fill(probabilidades, 50);
        return previsao(chuvaPorDia, tMin, tMax, probabilidades);
    }

    private static List<PrevisaoDiariaDTO> previsao(double[] chuvaPorDia, double tMin, double tMax,
                                                    int[] probabilidades) {
        List<PrevisaoDiariaDTO> lista = new ArrayList<>();
        for (int i = 0; i < chuvaPorDia.length; i++) {
            lista.add(new PrevisaoDiariaDTO(HOJE.plusDays(i), chuvaPorDia[i], probabilidades[i], tMax, tMin));
        }
        return lista;
    }
}
