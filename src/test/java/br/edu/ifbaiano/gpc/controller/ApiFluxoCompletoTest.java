package br.edu.ifbaiano.gpc.controller;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.service.OpenMeteoClient;
import br.edu.ifbaiano.gpc.frontcontroller.FrontControllerFilter;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre a API como o Postman faria: Front Controller (chave e erros),
 * DTOs, Repositories, Command e Strategy. A Open-Meteo é simulada.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiFluxoCompletoTest {

    private static final String CHAVE = "gpc-ifbaiano-2026";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FrontControllerFilter frontController;

    @MockitoBean
    private OpenMeteoClient openMeteoClient;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(frontController).build();
    }

    @Test
    void cicloCompletoDaSafra() throws Exception {
        List<PrevisaoDiariaDTO> previsao = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 16; i++) {
            previsao.add(new PrevisaoDiariaDTO(hoje.plusDays(i), i >= 2 ? 10 : 0, 80, 30, 20));
        }
        when(openMeteoClient.buscarPrevisaoDiaria(anyDouble(), anyDouble(), anyInt())).thenReturn(previsao);

        long produtorId = idCriado(json(post("/produtores"), """
                {"nome":"Ryan Haiala","cpfCnpj":"123.456.789-00","email":"ryan@email.com",
                 "municipio":"Guanambi","uf":"ba"}"""));
        mvc.perform(get("/produtores/" + produtorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpfCnpj").value("123.***.***-00"))
                .andExpect(jsonPath("$.uf").value("BA"));

        long talhaoId = idCriado(json(post("/talhoes"), """
                {"produtorId":%d,"nome":"Talhão Norte","areaHectares":12.5,
                 "latitude":-14.22,"longitude":-42.78,"tipoSolo":"ARGILOSO"}""".formatted(produtorId)));
        long culturaId = idCriado(json(post("/culturas"), """
                {"nome":"Milho","nomeCientifico":"Zea mays","cicloDias":120,"chuvaMinimaPlantioMm":20,
                 "temperaturaMinimaC":10,"temperaturaMaximaC":38,"produtividadeEsperadaKgHa":6000}"""));
        long safraId = idCriado(json(post("/safras"), """
                {"talhaoId":%d,"culturaId":%d}""".formatted(talhaoId, culturaId)));

        // RF12: segunda safra ativa no mesmo talhão
        mvc.perform(json(post("/safras"), """
                        {"talhaoId":%d,"culturaId":%d}""".formatted(talhaoId, culturaId)))
                .andExpect(status().isConflict());

        // Command + Strategy: 1º dia com 20 mm em 3 dias é hoje+1 (0 + 10 + 10)
        mvc.perform(post("/safras/" + safraId + "/sugestao-plantio").header("X-API-Key", CHAVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPlantioSugerida").value(hoje.plusDays(1).toString()))
                .andExpect(jsonPath("$.estrategia").value("chuva-acumulada"));

        mvc.perform(json(post("/safras/" + safraId + "/colheita"), """
                        {"dataColheita":"2027-03-15","producaoKg":70000}"""))
                .andExpect(status().isConflict());

        mvc.perform(json(post("/safras/" + safraId + "/plantio"), """
                        {"dataPlantio":"2026-11-12"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLANTADA"))
                .andExpect(jsonPath("$.dataColheitaPrevista").value("2027-03-12"))
                .andExpect(jsonPath("$.producaoEstimadaKg").value(75000.0));

        mvc.perform(json(post("/safras/" + safraId + "/colheita"), """
                        {"dataColheita":"2027-03-15","producaoKg":72000}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COLHIDA"));

        mvc.perform(get("/safras").param("status", "COLHIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/safras/comandos/historico"))
                .andExpect(jsonPath("$[0].comando").value("COLHER"))
                .andExpect(jsonPath("$.length()").value(4));

        // exclusões bloqueadas por integridade
        mvc.perform(delete("/produtores/" + produtorId).header("X-API-Key", CHAVE))
                .andExpect(status().isConflict());
        mvc.perform(delete("/safras/" + safraId).header("X-API-Key", CHAVE))
                .andExpect(status().isConflict());
    }

    @Test
    void frontControllerDeveExigirChaveNaEscrita() throws Exception {
        mvc.perform(post("/culturas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/culturas")).andExpect(status().isOk());
    }

    @Test
    void frontControllerDevePadronizarErros() throws Exception {
        mvc.perform(get("/produtores/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produtor com id 999 não encontrado(a)"));

        mvc.perform(json(post("/culturas"), """
                        {"nome":"","cicloDias":-1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cicloDias").exists());

        mvc.perform(json(post("/culturas"), """
                        {"nome":"Soja","cicloDias":110,"chuvaMinimaPlantioMm":25,
                         "temperaturaMinimaC":35,"temperaturaMaximaC":20,"produtividadeEsperadaKgHa":3500}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Temperatura mínima maior que a máxima"));

        mvc.perform(get("/safras").param("status", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarEExcluirCultura() throws Exception {
        long id = idCriado(json(post("/culturas"), """
                {"nome":"Feijão","cicloDias":90,"chuvaMinimaPlantioMm":15,
                 "temperaturaMinimaC":12,"temperaturaMaximaC":32,"produtividadeEsperadaKgHa":1800}"""));
        mvc.perform(json(put("/culturas/" + id), """
                        {"nome":"Feijão carioca","cicloDias":85,"chuvaMinimaPlantioMm":15,
                         "temperaturaMinimaC":12,"temperaturaMaximaC":32,"produtividadeEsperadaKgHa":2000}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Feijão carioca"))
                .andExpect(jsonPath("$.cicloDias").value(85));
        mvc.perform(delete("/culturas/" + id).header("X-API-Key", CHAVE))
                .andExpect(status().isNoContent());
        mvc.perform(get("/culturas/" + id)).andExpect(status().isNotFound());
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).header("X-API-Key", CHAVE).content(corpo);
    }

    private long idCriado(MockHttpServletRequestBuilder req) throws Exception {
        String corpo = mvc.perform(req)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }
}
