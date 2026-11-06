package br.edu.ifbaiano.gpc.model;

/**
 * Espécie cultivada (ex.: milho, feijão) e os parâmetros agronômicos
 * usados pela regra de sugestão da data de plantio.
 */
public class Cultura {

    private Long id;
    private String nome;
    private String nomeCientifico;
    /** Dias entre o plantio e a colheita. */
    private int cicloDias;
    /** Chuva acumulada mínima (mm) na janela de plantio para considerar o solo apto. */
    private double chuvaMinimaPlantioMm;
    private double temperaturaMinimaC;
    private double temperaturaMaximaC;
    /** Produtividade média esperada, em kg por hectare. */
    private double produtividadeEsperadaKgHa;

    public Cultura() {
    }

    public Cultura(Long id, String nome, String nomeCientifico, int cicloDias,
                   double chuvaMinimaPlantioMm, double temperaturaMinimaC,
                   double temperaturaMaximaC, double produtividadeEsperadaKgHa) {
        if (temperaturaMinimaC > temperaturaMaximaC) {
            throw new IllegalArgumentException("Temperatura mínima maior que a máxima");
        }
        this.id = id;
        this.nome = nome;
        this.nomeCientifico = nomeCientifico;
        setCicloDias(cicloDias);
        this.chuvaMinimaPlantioMm = chuvaMinimaPlantioMm;
        this.temperaturaMinimaC = temperaturaMinimaC;
        this.temperaturaMaximaC = temperaturaMaximaC;
        this.produtividadeEsperadaKgHa = produtividadeEsperadaKgHa;
    }

    /** Verifica se uma faixa de temperatura prevista é adequada para a cultura. */
    public boolean isTemperaturaAdequada(double minimaPrevista, double maximaPrevista) {
        return minimaPrevista >= temperaturaMinimaC && maximaPrevista <= temperaturaMaximaC;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeCientifico() {
        return nomeCientifico;
    }

    public void setNomeCientifico(String nomeCientifico) {
        this.nomeCientifico = nomeCientifico;
    }

    public int getCicloDias() {
        return cicloDias;
    }

    public void setCicloDias(int cicloDias) {
        if (cicloDias <= 0) {
            throw new IllegalArgumentException("O ciclo deve ter pelo menos 1 dia");
        }
        this.cicloDias = cicloDias;
    }

    public double getChuvaMinimaPlantioMm() {
        return chuvaMinimaPlantioMm;
    }

    public void setChuvaMinimaPlantioMm(double chuvaMinimaPlantioMm) {
        this.chuvaMinimaPlantioMm = chuvaMinimaPlantioMm;
    }

    public double getTemperaturaMinimaC() {
        return temperaturaMinimaC;
    }

    public void setTemperaturaMinimaC(double temperaturaMinimaC) {
        this.temperaturaMinimaC = temperaturaMinimaC;
    }

    public double getTemperaturaMaximaC() {
        return temperaturaMaximaC;
    }

    public void setTemperaturaMaximaC(double temperaturaMaximaC) {
        this.temperaturaMaximaC = temperaturaMaximaC;
    }

    public double getProdutividadeEsperadaKgHa() {
        return produtividadeEsperadaKgHa;
    }

    public void setProdutividadeEsperadaKgHa(double produtividadeEsperadaKgHa) {
        this.produtividadeEsperadaKgHa = produtividadeEsperadaKgHa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cultura outra)) return false;
        return id != null && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Cultura{id=" + id + ", nome='" + nome + "', cicloDias=" + cicloDias
                + ", chuvaMinimaPlantioMm=" + chuvaMinimaPlantioMm
                + ", temperatura=" + temperaturaMinimaC + ".." + temperaturaMaximaC + "}";
    }
}
