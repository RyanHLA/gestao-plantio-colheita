package br.edu.ifbaiano.gpc.service;

import br.edu.ifbaiano.gpc.dto.ColheitaRequestDTO;
import br.edu.ifbaiano.gpc.dto.HistoricoComandoDTO;
import br.edu.ifbaiano.gpc.dto.PlantioRequestDTO;
import br.edu.ifbaiano.gpc.dto.SafraRequestDTO;
import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.Cultura;
import br.edu.ifbaiano.gpc.model.Safra;
import br.edu.ifbaiano.gpc.model.StatusSafra;
import br.edu.ifbaiano.gpc.model.Talhao;
import br.edu.ifbaiano.gpc.repository.SafraRepository;
import br.edu.ifbaiano.gpc.command.CancelarSafraComando;
import br.edu.ifbaiano.gpc.command.ColherSafraComando;
import br.edu.ifbaiano.gpc.command.ExecutorComandoSafra;
import br.edu.ifbaiano.gpc.command.PlantarSafraComando;
import br.edu.ifbaiano.gpc.command.SugerirPlantioComando;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Regras de negócio das safras (RF04, RF06 a RF12).
 * As transições do ciclo de vida são despachadas como comandos (padrão Command).
 */
@Service
public class SafraService {

    private static final Set<StatusSafra> STATUS_ATIVOS = Set.of(StatusSafra.PLANEJADA, StatusSafra.PLANTADA);

    private final SafraRepository safraRepository;
    private final TalhaoService talhaoService;
    private final CulturaService culturaService;
    private final ProdutorService produtorService;
    private final SugestaoPlantioService sugestaoPlantioService;
    private final ExecutorComandoSafra executor;

    public SafraService(SafraRepository safraRepository, TalhaoService talhaoService,
                        CulturaService culturaService, ProdutorService produtorService,
                        SugestaoPlantioService sugestaoPlantioService, ExecutorComandoSafra executor) {
        this.safraRepository = safraRepository;
        this.talhaoService = talhaoService;
        this.culturaService = culturaService;
        this.produtorService = produtorService;
        this.sugestaoPlantioService = sugestaoPlantioService;
        this.executor = executor;
    }

    /** RF11: lista todas as safras ou filtra por status. */
    @Transactional(readOnly = true)
    public List<SafraResponseDTO> listar(StatusSafra status) {
        List<Safra> safras = status == null ? safraRepository.findAll() : safraRepository.findByStatus(status);
        return safras.stream().map(SafraResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public List<SafraResponseDTO> listarPorTalhao(Long talhaoId) {
        talhaoService.buscarEntidade(talhaoId);
        return safraRepository.findByTalhaoId(talhaoId).stream().map(SafraResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public List<SafraResponseDTO> listarPorProdutor(Long produtorId) {
        produtorService.buscarEntidade(produtorId);
        return safraRepository.findByTalhaoProdutorId(produtorId).stream().map(SafraResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public SafraResponseDTO buscar(Long id) {
        return SafraResponseDTO.de(buscarEntidade(id));
    }

    /** RF04 + RF12: planeja a safra, sem permitir duas safras ativas no mesmo talhão. */
    @Transactional
    public SafraResponseDTO planejar(SafraRequestDTO dto) {
        Talhao talhao = talhaoService.buscarEntidade(dto.talhaoId());
        Cultura cultura = culturaService.buscarEntidade(dto.culturaId());
        if (safraRepository.existsByTalhaoIdAndStatusIn(talhao.getId(), STATUS_ATIVOS)) {
            throw new RegraNegocioException("O talhão '" + talhao.getNome()
                    + "' já tem uma safra planejada ou plantada");
        }
        Safra safra = new Safra(null, talhao, cultura);
        talhao.adicionarSafra(safra);
        return SafraResponseDTO.de(safraRepository.save(safra));
    }

    /** Troca a cultura de uma safra ainda planejada. O talhão da safra não pode mudar. */
    @Transactional
    public SafraResponseDTO atualizar(Long id, SafraRequestDTO dto) {
        Safra safra = buscarEntidade(id);
        if (!safra.getTalhao().getId().equals(dto.talhaoId())) {
            throw new RegraNegocioException("O talhão de uma safra não pode ser alterado. "
                    + "Cancele esta safra e planeje outra no novo talhão.");
        }
        safra.trocarCultura(culturaService.buscarEntidade(dto.culturaId()));
        return SafraResponseDTO.de(safra);
    }

    @Transactional
    public void excluir(Long id) {
        Safra safra = buscarEntidade(id);
        if (!safra.isExcluivel()) {
            throw new RegraNegocioException("Só safras PLANEJADAS ou CANCELADAS podem ser excluídas. "
                    + "Esta safra está " + safra.getStatus());
        }
        safra.getTalhao().removerSafra(safra);
        safraRepository.delete(safra);
    }

    // ---- Comandos (padrão Command) ----

    public SugestaoPlantioDTO sugerirPlantio(Long id, String estrategia) {
        sugestaoPlantioService.estrategia(estrategia); // valida o código antes de abrir a transação
        return executor.executar(id, new SugerirPlantioComando(sugestaoPlantioService, estrategia));
    }

    public SafraResponseDTO plantar(Long id, PlantioRequestDTO dto) {
        return executor.executar(id, new PlantarSafraComando(dto.dataPlantio()));
    }

    public SafraResponseDTO colher(Long id, ColheitaRequestDTO dto) {
        return executor.executar(id, new ColherSafraComando(dto.dataColheita(), dto.producaoKg()));
    }

    public SafraResponseDTO cancelar(Long id) {
        return executor.executar(id, new CancelarSafraComando());
    }

    public List<HistoricoComandoDTO> historicoComandos() {
        return executor.historico();
    }

    private Safra buscarEntidade(Long id) {
        return safraRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Safra", id));
    }
}
