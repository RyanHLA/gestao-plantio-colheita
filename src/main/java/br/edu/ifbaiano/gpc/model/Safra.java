package br.edu.ifbaiano.gpc.model;

import java.time.LocalDate;

/**
 * Ciclo de produção de uma cultura em um talhão, do planejamento até a colheita.
 * É a entidade que relaciona Talhao e Cultura.
 */
public class Safra {

    private Long id;
    private Talhao talhao;
    private Cultura cultura;
    private LocalDate dataPlantioSugerida;
    private LocalDate dataPlantio;
    private LocalDate dataColheitaPrevista;
    private LocalDate dataColheita;
    private Double producaoKg;
    private StatusSafra status = StatusSafra.PLANEJADA;

    public Safra() {
    }

    public Safra(Long id, Talhao talhao, Cultura cultura) {
        this.id = id;
        this.talhao = talhao;
        this.cultura = cultura;
    }

    /** Registra a data sugerida pela regra de negócio (previsão de chuva). */
    public void registrarSugestaoPlantio(LocalDate dataSugerida) {
        exigirStatus(StatusSafra.PLANEJADA);
        this.dataPlantioSugerida = dataSugerida;
        this.dataColheitaPrevista = calcularDataColheita(dataSugerida);
    }

    /** Registra o plantio efetivo e recalcula a colheita prevista. */
    public void plantar(LocalDate data) {
        exigirStatus(StatusSafra.PLANEJADA);
        this.dataPlantio = data;
        this.dataColheitaPrevista = calcularDataColheita(data);
        this.status = StatusSafra.PLANTADA;
    }

    public void colher(LocalDate data, double producaoKg) {
        exigirStatus(StatusSafra.PLANTADA);
        if (data.isBefore(dataPlantio)) {
            throw new IllegalArgumentException("A colheita não pode ser antes do plantio");
        }
        this.dataColheita = data;
        this.producaoKg = producaoKg;
        this.status = StatusSafra.COLHIDA;
    }

    public void cancelar() {
        if (status == StatusSafra.COLHIDA) {
            throw new IllegalStateException("Safra já colhida não pode ser cancelada");
        }
        this.status = StatusSafra.CANCELADA;
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

    public void setCultura(Cultura cultura) {
        this.cultura = cultura;
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
        return id != null && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Safra{id=" + id
                + ", talhaoId=" + (talhao != null ? talhao.getId() : null)
                + ", cultura=" + (cultura != null ? cultura.getNome() : null)
                + ", status=" + status
                + ", dataPlantioSugerida=" + dataPlantioSugerida
                + ", dataPlantio=" + dataPlantio
                + ", dataColheitaPrevista=" + dataColheitaPrevista + "}";
    }
}
