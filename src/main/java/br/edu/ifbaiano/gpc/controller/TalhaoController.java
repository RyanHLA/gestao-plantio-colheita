package br.edu.ifbaiano.gpc.controller;

import br.edu.ifbaiano.gpc.dto.PrevisaoDiariaDTO;
import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.dto.TalhaoRequestDTO;
import br.edu.ifbaiano.gpc.dto.TalhaoResponseDTO;
import br.edu.ifbaiano.gpc.service.SafraService;
import br.edu.ifbaiano.gpc.service.TalhaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** RF02, RF05 e RF11 (safras por talhão). */
@RestController
@RequestMapping("/talhoes")
public class TalhaoController {

    private final TalhaoService talhaoService;
    private final SafraService safraService;

    public TalhaoController(TalhaoService talhaoService, SafraService safraService) {
        this.talhaoService = talhaoService;
        this.safraService = safraService;
    }

    @GetMapping
    public List<TalhaoResponseDTO> listar() {
        return talhaoService.listar();
    }

    @GetMapping("/{id}")
    public TalhaoResponseDTO buscar(@PathVariable Long id) {
        return talhaoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<TalhaoResponseDTO> criar(@Valid @RequestBody TalhaoRequestDTO dto) {
        TalhaoResponseDTO criado = talhaoService.criar(dto);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public TalhaoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody TalhaoRequestDTO dto) {
        return talhaoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        talhaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/safras")
    public List<SafraResponseDTO> safras(@PathVariable Long id) {
        return safraService.listarPorTalhao(id);
    }

    /** RF05: previsão do tempo de 16 dias para o talhão (Open-Meteo). */
    @GetMapping("/{id}/previsao")
    public List<PrevisaoDiariaDTO> previsao(@PathVariable Long id) {
        return talhaoService.previsao(id);
    }
}
