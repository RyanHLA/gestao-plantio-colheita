package br.edu.ifbaiano.gpc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Ciclo de produção de uma cultura em um talhão, do planejamento até a colheita.
 * É a entidade que relaciona Talhao e Cultura.
 */
@Entity
@Table(name = "safra")
public class Safra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "talhao_id", nullable = false)
    private Talhao talhao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cultura_id", nullable = false)
    private Cultura cultura;

    @Column(name = "data_plantio_sugerida")
    private LocalDate dataPlantioSugerida;

    @Column(name = "data_plantio")
    private LocalDate dataPlantio;

    @Column(name = "data_colheita_prevista")
    private LocalDate dataColheitaPrevista;

    @Column(name = "data_colheita")
    private LocalDate dataColheita;

    @Column(name = "producao_kg")
    private Double producaoKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSafra status = StatusSafra.PLANEJADA;

    public Safra() {
    }

    public Safra(Long id, Talhao talhao, Cultura cultura) {
        this.id = id;
        this.talhao = talhao;
        this.cultura = Objects.requireNonNull(cultura, "cultura");
    }

    /**
     * Registra a data sugerida pela regra de negócio (previsão de chuva).
     * Quando não há data sugerida (null), limpa a sugestão anterior.
     */
    public void registrarSugestaoPlantio(LocalDate dataSugerida) {
        exigirStatus(StatusSafra.PLANEJADA);
        this.dataPlantioSugerida = dataSugerida;
        this.dataColheitaPrevista = calcularDataColheita(dataSugerida);
    }

    /** Registra o plantio efetivo e recalcula a colheita prevista. */
    public void plantar(LocalDate data) {
        Objects.requireNonNull(data, "A data de plantio é obrigatória");
        exigirStatus(StatusSafra.PLANEJADA);
        this.dataPlantio = data;
        this.dataColheitaPrevista = calcularDataColheita(data);
        this.status = StatusSafra.PLANTADA;
    }

    public void colher(LocalDate data, double producaoKg) {
        Objects.requireNonNull(data, "A data de colheita é obrigatória");
        exigirStatus(StatusSafra.PLANTADA);
        if (data.isBefore(dataPlantio)) {
            throw new IllegalArgumentException("A colheita não pode ser antes do plantio");
        }
        if (producaoKg < 0) {
            throw new IllegalArgumentException("A produção não pode ser negativa");
        }
        this.dataColheita = data;
        this.producaoKg = producaoKg;
        this.status = StatusSafra.COLHIDA;
    }

    public void cancelar() {
        if (status == StatusSafra.COLHIDA || status == StatusSafra.CANCELADA) {
            throw new IllegalStateException("Safra " + status + " não pode ser cancelada");
        }
        this.status = StatusSafra.CANCELADA;
    }

    /** Troca a cultura enquanto a safra ainda está só planejada. A sugestão anterior perde a validade. */
    public void trocarCultura(Cultura novaCultura) {
        Objects.requireNonNull(novaCultura, "cultura");
        exigirStatus(StatusSafra.PLANEJADA);
        this.cultura = novaCultura;
        this.dataPlantioSugerida = null;
        this.dataColheitaPrevista = null;
    }

    /** Só safras planejadas (sem histórico) ou canceladas podem ser excluídas. */
    public boolean isExcluivel() {
        return status == StatusSafra.PLANEJADA || status == StatusSafra.CANCELADA;
    }

    /** Produção estimada = área do talhão x produtividade esperada da cultura. */
    public double getProducaoEstimadaKg() {
        if (talhao == null || cultura == null) {
            return 0;
        }
        return talhao.getAreaHectares() * cultura.getProdutividadeEsperadaKgHa();
    }

    public boolean isAtiva() {
        return status == StatusSafra.PLANEJADA || status == StatusSafra.PLANTADA;
    }

    private LocalDate calcularDataColheita(LocalDate dataPlantio) {
        if (dataPlantio == null || cultura == null) {
            return null;
        }
        return dataPlantio.plusDays(cultura.getCicloDias());
    }

    private void exigirStatus(StatusSafra esperado) {
        if (status != esperado) {
            throw new IllegalStateException(
                    "Operação exige status " + esperado + ", mas a safra está " + status);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Talhao getTalhao() {
        return talhao;
    }

    public void setTalhao(Talhao talhao) {
        this.talhao = talhao;
    }

    public Cultura getCultura() {
        return cultura;
    }

    public LocalDate getDataPlantioSugerida() {
        return dataPlantioSugerida;
    }

    public LocalDate getDataPlantio() {
        return dataPlantio;
    }

    public LocalDate getDataColheitaPrevista() {
        return dataColheitaPrevista;
    }

    public LocalDate getDataColheita() {
        return dataColheita;
    }

    public Double getProducaoKg() {
        return producaoKg;
    }

    public StatusSafra getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Safra outra)) return false;
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return Safra.class.hashCode();
    }

    @Override
    public String toString() {
        return "Safra{id=" + id + ", status=" + status
                + ", dataPlantioSugerida=" + dataPlantioSugerida
                + ", dataPlantio=" + dataPlantio
                + ", dataColheitaPrevista=" + dataColheitaPrevista + "}";
    }
}
