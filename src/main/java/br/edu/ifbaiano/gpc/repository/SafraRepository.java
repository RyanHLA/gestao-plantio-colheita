package br.edu.ifbaiano.gpc.repository;

import br.edu.ifbaiano.gpc.model.Safra;
import br.edu.ifbaiano.gpc.model.StatusSafra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/** Padrão Repository/DAO para as safras. */
public interface SafraRepository extends JpaRepository<Safra, Long> {

    List<Safra> findByStatus(StatusSafra status);

    List<Safra> findByTalhaoId(Long talhaoId);

    List<Safra> findByTalhaoProdutorId(Long produtorId);

    boolean existsByTalhaoIdAndStatusIn(Long talhaoId, Collection<StatusSafra> status);

    boolean existsByCulturaId(Long culturaId);
}
