package br.edu.ifbaiano.gpc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Área delimitada da propriedade onde ocorre o plantio.
 * A latitude e a longitude são enviadas à API Open-Meteo para obter a previsão do tempo.
 */
public class Talhao {

    private Long id;
    private String nome;
    private double areaHectares;
    private double latitude;
    private double longitude;
    private TipoSolo tipoSolo;
    private Produtor produtor;
    private final List<Safra> safras = new ArrayList<>();

    public Talhao() {
    }

    public Talhao(Long id, String nome, double areaHectares, double latitude,
                  double longitude, TipoSolo tipoSolo) {
        this.id = id;
        this.nome = nome;
        this.areaHectares = areaHectares;
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

    /** Indica se o talhão já tem uma safra planejada ou em andamento. */
    public boolean isOcupado() {
        return safras.stream().anyMatch(Safra::isAtiva);
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
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Talhao{id=" + id + ", nome='" + nome + "', areaHectares=" + areaHectares
                + ", latitude=" + latitude + ", longitude=" + longitude
                + ", tipoSolo=" + tipoSolo
                + ", produtorId=" + (produtor != null ? produtor.getId() : null) + "}";
    }
}
