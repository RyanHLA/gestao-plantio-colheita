package br.edu.ifbaiano.gpc.repository;

import br.edu.ifbaiano.gpc.model.Cultura;
import org.springframework.data.jpa.repository.JpaRepository;

/** Padrão Repository/DAO para as culturas. */
public interface CulturaRepository extends JpaRepository<Cultura, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
