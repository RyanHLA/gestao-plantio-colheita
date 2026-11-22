package br.edu.ifbaiano.gpc.controller;

import br.edu.ifbaiano.gpc.dto.ColheitaRequestDTO;
import br.edu.ifbaiano.gpc.dto.EstrategiaSugestaoDTO;
import br.edu.ifbaiano.gpc.dto.HistoricoComandoDTO;
import br.edu.ifbaiano.gpc.dto.PlantioRequestDTO;
import br.edu.ifbaiano.gpc.dto.SafraRequestDTO;
import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.dto.SugestaoPlantioDTO;
import br.edu.ifbaiano.gpc.model.StatusSafra;
import br.edu.ifbaiano.gpc.service.SafraService;
import br.edu.ifbaiano.gpc.service.SugestaoPlantioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * RF04 e RF06 a RF12. As rotas de ação (sugestão, plantio, colheita, cancelamento)
 * disparam comandos do padrão Command no {@link SafraService}.
 */
@RestController
@RequestMapping("/safras")
public class SafraController {

    private final SafraService safraService;
    private final SugestaoPlantioService sugestaoPlantioService;

    public SafraController(SafraService safraService, SugestaoPlantioService sugestaoPlantioService) {
        this.safraService = safraService;
        this.sugestaoPlantioService = sugestaoPlantioService;
    }

    /** RF11: GET /safras ou GET /safras?status=PLANTADA */
    @GetMapping
    public List<SafraResponseDTO> listar(@RequestParam(required = false) StatusSafra status) {
        return safraService.listar(status);
    }

    @GetMapping("/{id}")
    public SafraResponseDTO buscar(@PathVariable Long id) {
        return safraService.buscar(id);
    }

    /** RF04: planeja uma safra ligando um talhão a uma cultura. */
    @PostMapping
    public ResponseEntity<SafraResponseDTO> planejar(@Valid @RequestBody SafraRequestDTO dto) {
        SafraResponseDTO criada = safraService.planejar(dto);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    /** Troca a cultura de uma safra ainda PLANEJADA. */
    @PutMapping("/{id}")
    public SafraResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody SafraRequestDTO dto) {
        return safraService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        safraService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Ações (padrão Command) ----

    /** RF06 e RF07. Estratégias: GET /safras/estrategias-sugestao */
    @PostMapping("/{id}/sugestao-plantio")
    public SugestaoPlantioDTO sugerirPlantio(@PathVariable Long id,
                                             @RequestParam(required = false) String estrategia) {
        return safraService.sugerirPlantio(id, estrategia);
    }

    /** RF08 */
    @PostMapping("/{id}/plantio")
    public SafraResponseDTO plantar(@PathVariable Long id, @Valid @RequestBody PlantioRequestDTO dto) {
        return safraService.plantar(id, dto);
    }

    /** RF09 */
    @PostMapping("/{id}/colheita")
    public SafraResponseDTO colher(@PathVariable Long id, @Valid @RequestBody ColheitaRequestDTO dto) {
        return safraService.colher(id, dto);
    }

    @PostMapping("/{id}/cancelamento")
    public SafraResponseDTO cancelar(@PathVariable Long id) {
        return safraService.cancelar(id);
    }

    /** Histórico dos comandos executados (sucesso e falha), mais recentes primeiro. */
    @GetMapping("/comandos/historico")
    public List<HistoricoComandoDTO> historicoComandos() {
        return safraService.historicoComandos();
    }

    /** Estratégias de sugestão de plantio disponíveis (padrão Strategy). */
    @GetMapping("/estrategias-sugestao")
    public List<EstrategiaSugestaoDTO> estrategias() {
        return sugestaoPlantioService.listarEstrategias();
    }
}
