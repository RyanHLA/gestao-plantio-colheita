package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.ProdutorRequestDTO;
import br.edu.ifbaiano.gpc.dto.ProdutorResponseDTO;
import br.edu.ifbaiano.gpc.model.Produtor;
import br.edu.ifbaiano.gpc.repository.ProdutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negócio dos produtores (RF01). */
@Service
public class ProdutorService {

    private final ProdutorRepository produtorRepository;

    public ProdutorService(ProdutorRepository produtorRepository) {
        this.produtorRepository = produtorRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutorResponseDTO> listar() {
        return produtorRepository.findAll().stream().map(ProdutorResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public ProdutorResponseDTO buscar(Long id) {
        return ProdutorResponseDTO.de(buscarEntidade(id));
    }

    @Transactional
    public ProdutorResponseDTO criar(ProdutorRequestDTO dto) {
        String documento = normalizarDocumento(dto.cpfCnpj());
        if (produtorRepository.existsByCpfCnpj(documento)) {
            throw new RegraNegocioException("Já existe produtor com este CPF/CNPJ");
        }
        Produtor produtor = new Produtor();
        copiar(dto, documento, produtor);
        return ProdutorResponseDTO.de(produtorRepository.save(produtor));
    }

    @Transactional
    public ProdutorResponseDTO atualizar(Long id, ProdutorRequestDTO dto) {
        Produtor produtor = buscarEntidade(id);
        String documento = normalizarDocumento(dto.cpfCnpj());
        if (produtorRepository.existsByCpfCnpjAndIdNot(documento, id)) {
            throw new RegraNegocioException("Já existe outro produtor com este CPF/CNPJ");
        }
        copiar(dto, documento, produtor);
        return ProdutorResponseDTO.de(produtor);
    }

    @Transactional
    public void excluir(Long id) {
        Produtor produtor = buscarEntidade(id);
        if (produtor.possuiTalhoes()) {
            throw new RegraNegocioException("Produtor possui talhões cadastrados. Exclua os talhões antes.");
        }
        produtorRepository.delete(produtor);
    }

    Produtor buscarEntidade(Long id) {
        return produtorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produtor", id));
    }

    private static void copiar(ProdutorRequestDTO dto, String documento, Produtor produtor) {
        produtor.setNome(dto.nome().trim());
        produtor.setCpfCnpj(documento);
        produtor.setEmail(dto.email());
        produtor.setTelefone(dto.telefone());
        produtor.setMunicipio(dto.municipio());
        produtor.setUf(dto.uf() != null ? dto.uf().toUpperCase() : null);
    }

    /** Guarda só os dígitos, para que "123.456.789-00" e "12345678900" sejam o mesmo documento. */
    private static String normalizarDocumento(String cpfCnpj) {
        return cpfCnpj.replaceAll("\\D", "");
    }
}
