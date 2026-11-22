package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.TalhaoRequestDTO;
import br.edu.ifbaiano.gpc.dto.TalhaoResponseDTO;
import br.edu.ifbaiano.gpc.model.Produtor;
import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.repository.TalhaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negócio dos talhões (RF02 e RF05). */
@Service
public class TalhaoService {

    private final TalhaoRepository talhaoRepository;
    private final ProdutorService produtorService;
    private final SugestaoPlantioService sugestaoPlantioService;

    public TalhaoService(TalhaoRepository talhaoRepository, ProdutorService produtorService,
                         SugestaoPlantioService sugestaoPlantioService) {
        this.talhaoRepository = talhaoRepository;
        this.produtorService = produtorService;
        this.sugestaoPlantioService = sugestaoPlantioService;
    }

    @Transactional(readOnly = true)
    public List<TalhaoResponseDTO> listar() {
        return talhaoRepository.findAll().stream().map(TalhaoResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public List<TalhaoResponseDTO> listarPorProdutor(Long produtorId) {
        produtorService.buscarEntidade(produtorId);
        return talhaoRepository.findByProdutorId(produtorId).stream().map(TalhaoResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public TalhaoResponseDTO buscar(Long id) {
        return TalhaoResponseDTO.de(buscarEntidade(id));
    }

    @Transactional
    public TalhaoResponseDTO criar(TalhaoRequestDTO dto) {
        Produtor produtor = produtorService.buscarEntidade(dto.produtorId());
        Talhao talhao = new Talhao();
        copiar(dto, talhao);
        produtor.adicionarTalhao(talhao);
        return TalhaoResponseDTO.de(talhaoRepository.save(talhao));
    }

    @Transactional
    public TalhaoResponseDTO atualizar(Long id, TalhaoRequestDTO dto) {
        Talhao talhao = buscarEntidade(id);
        copiar(dto, talhao);
        if (!dto.produtorId().equals(talhao.getProdutor().getId())) {
            produtorService.buscarEntidade(dto.produtorId()).adicionarTalhao(talhao);
        }
        return TalhaoResponseDTO.de(talhao);
    }

    @Transactional
    public void excluir(Long id) {
        Talhao talhao = buscarEntidade(id);
        if (talhao.possuiSafras()) {
            throw new RegraNegocioException("Talhão possui safras registradas e não pode ser excluído");
        }
        talhao.getProdutor().removerTalhao(talhao);
        talhaoRepository.delete(talhao);
    }

    /** RF05: previsão de 16 dias para a localização do talhão. */
    @Transactional(readOnly = true)
    public List<PrevisaoDiariaDTO> previsao(Long id) {
        return sugestaoPlantioService.buscarPrevisao(buscarEntidade(id));
    }

    Talhao buscarEntidade(Long id) {
        return talhaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Talhão", id));
    }

    private static void copiar(TalhaoRequestDTO dto, Talhao talhao) {
        talhao.setNome(dto.nome().trim());
        talhao.setAreaHectares(dto.areaHectares());
        talhao.setLatitude(dto.latitude());
        talhao.setLongitude(dto.longitude());
        talhao.setTipoSolo(dto.tipoSolo());
    }
}
