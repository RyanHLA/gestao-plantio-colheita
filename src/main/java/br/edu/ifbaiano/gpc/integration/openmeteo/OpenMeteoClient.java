package br.edu.ifbaiano.gpc.integration.openmeteo;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cliente da API pública Open-Meteo (https://open-meteo.com). Não exige chave de acesso.
 */
@Component
public class OpenMeteoClient {

    public static final String URL_PADRAO = "https://api.open-meteo.com";
    /** Máximo de dias de previsão aceito pela API. */
    public static final int MAX_DIAS_PREVISAO = 16;

    private static final String VARIAVEIS_DIARIAS =
            "precipitation_sum,precipitation_probability_max,temperature_2m_max,temperature_2m_min";

    private final RestClient restClient;

    public OpenMeteoClient(RestClient.Builder builder,
                           @Value("${openmeteo.base-url:" + URL_PADRAO + "}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Busca a previsão diária para as coordenadas informadas.
     *
     * @param dias quantidade de dias (1 a 16)
     */
    public List<PrevisaoDiariaDTO> buscarPrevisaoDiaria(double latitude, double longitude, int dias) {
        if (dias < 1 || dias > MAX_DIAS_PREVISAO) {
            throw new IllegalArgumentException("dias deve estar entre 1 e " + MAX_DIAS_PREVISAO);
        }

        OpenMeteoResponse resposta = restClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", String.format(Locale.ROOT, "%.4f", latitude))
                        .queryParam("longitude", String.format(Locale.ROOT, "%.4f", longitude))
                        .queryParam("daily", VARIAVEIS_DIARIAS)
                        .queryParam("timezone", "America/Sao_Paulo")
                        .queryParam("forecast_days", dias)
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        return converter(resposta);
    }

    static List<PrevisaoDiariaDTO> converter(OpenMeteoResponse resposta) {
        if (resposta == null || resposta.daily() == null || resposta.daily().time() == null) {
            throw new OpenMeteoException("Resposta da Open-Meteo sem dados diários");
        }
        OpenMeteoResponse.Daily d = resposta.daily();
        List<PrevisaoDiariaDTO> previsoes = new ArrayList<>();
        for (int i = 0; i < d.time().size(); i++) {
            previsoes.add(new PrevisaoDiariaDTO(
                    LocalDate.parse(d.time().get(i)),
                    valorOuZero(d.precipitationSum(), i),
                    valorOuNull(d.precipitationProbabilityMax(), i),
                    valorOuZero(d.temperatureMax(), i),
                    valorOuZero(d.temperatureMin(), i)));
        }
        return previsoes;
    }

    private static double valorOuZero(List<Double> lista, int i) {
        Double v = valorOuNull(lista, i);
        return v != null ? v : 0.0;
    }

    private static <T> T valorOuNull(List<T> lista, int i) {
        return (lista != null && i < lista.size()) ? lista.get(i) : null;
    }
}
