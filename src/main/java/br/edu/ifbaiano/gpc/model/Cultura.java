package br.edu.ifbaiano.gpc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Espécie cultivada (ex.: milho, feijão) e os parâmetros agronômicos
 * usados pela regra de sugestão da data de plantio.
 */
@Entity
@Table(name = "cultura")
public class Cultura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Column(name = "nome_cientifico", length = 120)
    private String nomeCientifico;

    /** Dias entre o plantio e a colheita. */
    @Column(name = "ciclo_dias", nullable = false)
    private int cicloDias;

    /** Chuva acumulada mínima (mm) na janela de plantio para considerar o solo apto. */
    @Column(name = "chuva_minima_plantio_mm", nullable = false)
    private double chuvaMinimaPlantioMm;

    @Column(name = "temperatura_minima_c")
    private double temperaturaMinimaC;

    @Column(name = "temperatura_maxima_c")
    private double temperaturaMaximaC;

    /** Produtividade média esperada, em kg por hectare. */
    @Column(name = "produtividade_esperada_kg_ha")
    private double produtividadeEsperadaKgHa;

    public Cultura() {
    }

    public Cultura(Long id, String nome, String nomeCientifico, int cicloDias,
                   double chuvaMinimaPlantioMm, double temperaturaMinimaC,
                   double temperaturaMaximaC, double produtividadeEsperadaKgHa) {
        this.id = id;
        this.nome = nome;
        this.nomeCientifico = nomeCientifico;
        setCicloDias(cicloDias);
        setChuvaMinimaPlantioMm(chuvaMinimaPlantioMm);
        definirFaixaTemperatura(temperaturaMinimaC, temperaturaMaximaC);
        setProdutividadeEsperadaKgHa(produtividadeEsperadaKgHa);
    }

    /** Verifica se uma faixa de temperatura prevista é adequada para a cultura. */
    public boolean isTemperaturaAdequada(double minimaPrevista, double maximaPrevista) {
        return minimaPrevista >= temperaturaMinimaC && maximaPrevista <= temperaturaMaximaC;
    }

    /** Define mínima e máxima juntas, para nunca deixar a faixa invertida. */
    public void definirFaixaTemperatura(double minimaC, double maximaC) {
        if (minimaC > maximaC) {
            throw new IllegalArgumentException("Temperatura mínima maior que a máxima");
        }
        this.temperaturaMinimaC = minimaC;
        this.temperaturaMaximaC = maximaC;
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
        if (chuvaMinimaPlantioMm < 0) {
            throw new IllegalArgumentException("A chuva mínima de plantio não pode ser negativa");
        }
        this.chuvaMinimaPlantioMm = chuvaMinimaPlantioMm;
    }

    public double getTemperaturaMinimaC() {
        return temperaturaMinimaC;
    }

    public double getTemperaturaMaximaC() {
        return temperaturaMaximaC;
    }

    public double getProdutividadeEsperadaKgHa() {
        return produtividadeEsperadaKgHa;
    }

    public void setProdutividadeEsperadaKgHa(double produtividadeEsperadaKgHa) {
        if (produtividadeEsperadaKgHa < 0) {
            throw new IllegalArgumentException("A produtividade esperada não pode ser negativa");
        }
        this.produtividadeEsperadaKgHa = produtividadeEsperadaKgHa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cultura outra)) return false;
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return Cultura.class.hashCode();
    }

    @Override
    public String toString() {
        return "Cultura{id=" + id + ", nome='" + nome + "', cicloDias=" + cicloDias
                + ", chuvaMinimaPlantioMm=" + chuvaMinimaPlantioMm
                + ", temperatura=" + temperaturaMinimaC + ".." + temperaturaMaximaC + "}";
    }
}
