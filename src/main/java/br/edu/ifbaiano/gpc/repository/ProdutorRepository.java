package br.edu.ifbaiano.gpc.repository;

import br.edu.ifbaiano.gpc.model.Produtor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Padrão Repository/DAO: isola o acesso ao banco de dados.
 * O Spring Data JPA gera a implementação a partir da interface.
 */
public interface ProdutorRepository extends JpaRepository<Produtor, Long> {

    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);
}
