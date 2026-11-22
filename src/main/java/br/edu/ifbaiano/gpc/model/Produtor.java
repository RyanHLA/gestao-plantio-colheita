package br.edu.ifbaiano.gpc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Produtor rural dono de um ou mais talhões.
 */
@Entity
@Table(name = "produtor")
public class Produtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 18)
    private String cpfCnpj;

    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 80)
    private String municipio;

    @Column(length = 2)
    private String uf;

    @OneToMany(mappedBy = "produtor")
    private List<Talhao> talhoes = new ArrayList<>();

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
        if (talhao.getProdutor() != null && talhao.getProdutor() != this) {
            talhao.getProdutor().removerTalhao(talhao);
        }
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

    public boolean possuiTalhoes() {
        return !talhoes.isEmpty();
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
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Produtor.class.hashCode();
    }

    @Override
    public String toString() {
        return "Produtor{id=" + id + ", nome='" + nome + "', municipio='" + municipio
                + "', uf='" + uf + "'}";
    }
}
