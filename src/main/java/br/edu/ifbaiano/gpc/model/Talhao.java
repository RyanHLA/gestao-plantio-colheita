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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Área delimitada da propriedade onde ocorre o plantio.
 * A latitude e a longitude são enviadas à API Open-Meteo para obter a previsão do tempo.
 */
@Entity
@Table(name = "talhao")
public class Talhao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "area_hectares", nullable = false)
    private double areaHectares;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solo", length = 20)
    private TipoSolo tipoSolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produtor_id", nullable = false)
    private Produtor produtor;

    @OneToMany(mappedBy = "talhao")
    private List<Safra> safras = new ArrayList<>();

    public Talhao() {
    }

    public Talhao(Long id, String nome, double areaHectares, double latitude,
                  double longitude, TipoSolo tipoSolo) {
        this.id = id;
        this.nome = nome;
        setAreaHectares(areaHectares);
        setLatitude(latitude);
        setLongitude(longitude);
        this.tipoSolo = tipoSolo;
    }

    public void adicionarSafra(Safra safra) {
        Objects.requireNonNull(safra, "safra");
        if (!safras.contains(safra)) {
            safras.add(safra);
        }
        safra.setTalhao(this);
    }

    public void removerSafra(Safra safra) {
        safras.remove(safra);
    }

    /** Indica se o talhão já tem uma safra planejada ou em andamento. */
    public boolean isOcupado() {
        return safras.stream().anyMatch(Safra::isAtiva);
    }

    public boolean possuiSafras() {
        return !safras.isEmpty();
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

    public double getAreaHectares() {
        return areaHectares;
    }

    public void setAreaHectares(double areaHectares) {
        if (areaHectares <= 0) {
            throw new IllegalArgumentException("A área do talhão deve ser maior que zero");
        }
        this.areaHectares = areaHectares;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude deve estar entre -90 e 90");
        }
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude deve estar entre -180 e 180");
        }
        this.longitude = longitude;
    }

    public TipoSolo getTipoSolo() {
        return tipoSolo;
    }

    public void setTipoSolo(TipoSolo tipoSolo) {
        this.tipoSolo = tipoSolo;
    }

    public Produtor getProdutor() {
        return produtor;
    }

    public void setProdutor(Produtor produtor) {
        this.produtor = produtor;
    }

    public List<Safra> getSafras() {
        return Collections.unmodifiableList(safras);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Talhao outro)) return false;
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Talhao.class.hashCode();
    }

    @Override
    public String toString() {
        return "Talhao{id=" + id + ", nome='" + nome + "', areaHectares=" + areaHectares
                + ", latitude=" + latitude + ", longitude=" + longitude
                + ", tipoSolo=" + tipoSolo + "}";
    }
}
