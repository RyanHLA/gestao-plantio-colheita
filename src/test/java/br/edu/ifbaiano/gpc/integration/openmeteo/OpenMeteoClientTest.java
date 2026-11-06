package br.edu.ifbaiano.gpc.integration.openmeteo;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.model.TipoSolo;
import br.edu.ifbaiano.gpc.service.SugestaoPlantioService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste isolado (sem subir o Spring) que demonstra a viabilidade do consumo
 * da API Open-Meteo. Faz uma requisição HTTP real: precisa de acesso à internet.
 */
class OpenMeteoClientTest {

    // Campus Guanambi do IF Baiano
    private static final double LATITUDE = -14.2233;
    private static final double LONGITUDE = -42.7814;

    private final OpenMeteoClient client =
            new OpenMeteoClient(RestClient.builder(), OpenMeteoClient.URL_PADRAO);

    @Test
    void deveBuscarPrevisaoDiariaDe16Dias() {
        List<PrevisaoDiariaDTO> previsao = client.buscarPrevisaoDiaria(LATITUDE, LONGITUDE, 16);

        assertEquals(16, previsao.size());
        assertFalse(previsao.getFirst().data().isBefore(LocalDate.now().minusDays(1)));
        previsao.forEach(dia -> {
            assertNotNull(dia.data());
            assertTrue(dia.precipitacaoMm() >= 0);
            assertTrue(dia.temperaturaMaximaC() >= dia.temperaturaMinimaC());
        });

        System.out.println("Previsão Open-Meteo para Guanambi-BA:");
        previsao.forEach(dia -> System.out.printf("  %s  chuva=%5.1f mm  prob=%s%%  temp=%.1f..%.1f °C%n",
                dia.data(), dia.precipitacaoMm(), dia.probabilidadeChuvaPercentual(),
                dia.temperaturaMinimaC(), dia.temperaturaMaximaC()));
    }

    @Test
    void deveSugerirDataDePlantioComDadosReaisDaApi() {
        Talhao talhao = new Talhao(1L, "Talhão Norte", 12.5, LATITUDE, LONGITUDE, TipoSolo.ARGILOSO);
        Cultura milho = new Cultura(1L, "Milho", "Zea mays", 120, 20.0, 10.0, 38.0, 6000);

        SugestaoPlantioDTO sugestao = new SugestaoPlantioService(client).sugerir(talhao, milho);

        assertNotNull(sugestao.justificativa());
        if (sugestao.encontrouData()) {
            assertEquals(sugestao.dataPlantioSugerida().plusDays(120), sugestao.dataColheitaPrevista());
        }
        System.out.println("Sugestão: " + sugestao);
    }
}
