package br.edu.ifbaiano.gpc.repository;

import br.edu.ifbaiano.gpc.model.Talhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Padrão Repository/DAO para os talhões. */
public interface TalhaoRepository extends JpaRepository<Talhao, Long> {

    List<Talhao> findByProdutorId(Long produtorId);
}
