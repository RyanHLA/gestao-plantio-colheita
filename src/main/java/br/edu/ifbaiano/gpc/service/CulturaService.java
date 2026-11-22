package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.CulturaRequestDTO;
import br.edu.ifbaiano.gpc.dto.CulturaResponseDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.repository.CulturaRepository;
import br.edu.ifbaiano.gpc.repository.SafraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negócio das culturas (RF03). */
@Service
public class CulturaService {

    private final CulturaRepository culturaRepository;
    private final SafraRepository safraRepository;

    public CulturaService(CulturaRepository culturaRepository, SafraRepository safraRepository) {
        this.culturaRepository = culturaRepository;
        this.safraRepository = safraRepository;
    }

    @Transactional(readOnly = true)
    public List<CulturaResponseDTO> listar() {
        return culturaRepository.findAll().stream().map(CulturaResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public CulturaResponseDTO buscar(Long id) {
        return CulturaResponseDTO.de(buscarEntidade(id));
    }

    @Transactional
    public CulturaResponseDTO criar(CulturaRequestDTO dto) {
        if (culturaRepository.existsByNomeIgnoreCase(dto.nome().trim())) {
            throw new RegraNegocioException("Já existe cultura com o nome " + dto.nome());
        }
        Cultura cultura = new Cultura();
        copiar(dto, cultura);
        return CulturaResponseDTO.de(culturaRepository.save(cultura));
    }

    @Transactional
    public CulturaResponseDTO atualizar(Long id, CulturaRequestDTO dto) {
        Cultura cultura = buscarEntidade(id);
        if (culturaRepository.existsByNomeIgnoreCaseAndIdNot(dto.nome().trim(), id)) {
            throw new RegraNegocioException("Já existe outra cultura com o nome " + dto.nome());
        }
        copiar(dto, cultura);
        return CulturaResponseDTO.de(cultura);
    }

    @Transactional
    public void excluir(Long id) {
        Cultura cultura = buscarEntidade(id);
        if (safraRepository.existsByCulturaId(id)) {
            throw new RegraNegocioException("Cultura usada em safras não pode ser excluída");
        }
        culturaRepository.delete(cultura);
    }

    Cultura buscarEntidade(Long id) {
        return culturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cultura", id));
    }

    private static void copiar(CulturaRequestDTO dto, Cultura cultura) {
        cultura.setNome(dto.nome().trim());
        cultura.setNomeCientifico(dto.nomeCientifico());
        cultura.setCicloDias(dto.cicloDias());
        cultura.setChuvaMinimaPlantioMm(dto.chuvaMinimaPlantioMm());
        cultura.definirFaixaTemperatura(dto.temperaturaMinimaC(), dto.temperaturaMaximaC());
        cultura.setProdutividadeEsperadaKgHa(dto.produtividadeEsperadaKgHa());
    }
}
