package br.edu.ifbaiano.gpc.controller;

import br.edu.ifbaiano.gpc.dto.CulturaRequestDTO;
import br.edu.ifbaiano.gpc.dto.CulturaResponseDTO;
import br.edu.ifbaiano.gpc.service.CulturaService;
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

/** RF03. */
@RestController
@RequestMapping("/culturas")
public class CulturaController {

    private final CulturaService culturaService;

    public CulturaController(CulturaService culturaService) {
        this.culturaService = culturaService;
    }

    @GetMapping
    public List<CulturaResponseDTO> listar() {
        return culturaService.listar();
    }

    @GetMapping("/{id}")
    public CulturaResponseDTO buscar(@PathVariable Long id) {
        return culturaService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CulturaResponseDTO> criar(@Valid @RequestBody CulturaRequestDTO dto) {
        CulturaResponseDTO criada = culturaService.criar(dto);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    @PutMapping("/{id}")
    public CulturaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody CulturaRequestDTO dto) {
        return culturaService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        culturaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
