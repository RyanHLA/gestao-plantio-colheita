package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.model.TipoSolo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Testa a regra de negócio com previsões fixas (sem acesso à rede). */
class SugestaoPlantioServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 6);

    private final SugestaoPlantioService service = new SugestaoPlantioService(null);
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

    private static List<PrevisaoDiariaDTO> previsao(double[] chuvaPorDia, double tMin, double tMax) {
        List<PrevisaoDiariaDTO> lista = new ArrayList<>();
        for (int i = 0; i < chuvaPorDia.length; i++) {
            lista.add(new PrevisaoDiariaDTO(HOJE.plusDays(i), chuvaPorDia[i], 50, tMax, tMin));
        }
        return lista;
    }
}
