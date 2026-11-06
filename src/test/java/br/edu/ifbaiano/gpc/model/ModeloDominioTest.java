package br.edu.ifbaiano.gpc.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeloDominioTest {

    private final Produtor produtor = new Produtor(1L, "Ryan Haiala", "123.456.789-00",
            "ryan@email.com", "(77) 99999-0000", "Guanambi", "BA");
    private final Talhao talhao = new Talhao(1L, "Talhão Norte", 12.5, -14.22, -42.78, TipoSolo.ARGILOSO);
    private final Cultura milho = new Cultura(1L, "Milho", "Zea mays", 120, 20.0, 10.0, 38.0, 6000);

    @Test
    void produtorDeveManterAssociacaoComTalhoes() {
        produtor.adicionarTalhao(talhao);
        produtor.adicionarTalhao(new Talhao(2L, "Talhão Sul", 7.5, -14.23, -42.79, TipoSolo.ARENOSO));

        assertEquals(2, produtor.getTalhoes().size());
        assertSame(produtor, talhao.getProdutor());
        assertEquals(20.0, produtor.getAreaTotalHectares(), 0.001);
    }

    @Test
    void safraDeveSeguirCicloPlanejadaPlantadaColhida() {
        Safra safra = new Safra(1L, talhao, milho);
        talhao.adicionarSafra(safra);
        assertTrue(talhao.isOcupado());

        safra.registrarSugestaoPlantio(LocalDate.of(2026, 11, 10));
        assertEquals(LocalDate.of(2027, 3, 10), safra.getDataColheitaPrevista());

        safra.plantar(LocalDate.of(2026, 11, 12));
        assertEquals(StatusSafra.PLANTADA, safra.getStatus());
        assertEquals(LocalDate.of(2027, 3, 12), safra.getDataColheitaPrevista());
        assertEquals(75_000, safra.getProducaoEstimadaKg(), 0.001);

        safra.colher(LocalDate.of(2027, 3, 15), 72_000);
        assertEquals(StatusSafra.COLHIDA, safra.getStatus());
        assertFalse(talhao.isOcupado());
    }

    @Test
    void naoDeveColherSafraQueNaoFoiPlantada() {
        Safra safra = new Safra(1L, talhao, milho);
        assertThrows(IllegalStateException.class, () -> safra.colher(LocalDate.now(), 100));
    }

    @Test
    void deveValidarCoordenadasDoTalhao() {
        assertThrows(IllegalArgumentException.class, () -> talhao.setLatitude(-91));
        assertThrows(IllegalArgumentException.class, () -> talhao.setLongitude(181));
    }
}
