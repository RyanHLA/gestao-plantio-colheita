package br.edu.ifbaiano.gpc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Produtor rural dono de um ou mais talhões.
 */
public class Produtor {

    private Long id;
    private String nome;
    private String cpfCnpj;
    private String email;
    private String telefone;
    private String municipio;
    private String uf;
    private final List<Talhao> talhoes = new ArrayList<>();

    public Produtor() {
    }

    public Produtor(Long id, String nome, String cpfCnpj, String email,
                    String telefone, String municipio, String uf) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.telefone = telefone;
        this.municipio = municipio;
        this.uf = uf;
    }

    /** Vincula o talhão a este produtor (mantém os dois lados da associação). */
    public void adicionarTalhao(Talhao talhao) {
        Objects.requireNonNull(talhao, "talhao");
        if (!talhoes.contains(talhao)) {
            talhoes.add(talhao);
        }
        talhao.setProdutor(this);
    }

    public void removerTalhao(Talhao talhao) {
        if (talhoes.remove(talhao)) {
            talhao.setProdutor(null);
        }
    }

    /** Soma das áreas de todos os talhões do produtor, em hectares. */
    public double getAreaTotalHectares() {
        return talhoes.stream().mapToDouble(Talhao::getAreaHectares).sum();
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

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public List<Talhao> getTalhoes() {
        return Collections.unmodifiableList(talhoes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produtor outro)) return false;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Produtor{id=" + id + ", nome='" + nome + "', cpfCnpj='" + cpfCnpj
                + "', municipio='" + municipio + "', uf='" + uf
                + "', talhoes=" + talhoes.size() + "}";
    }
}
