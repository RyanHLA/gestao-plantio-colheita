package br.edu.ifbaiano.gpc.controller;

import br.edu.ifbaiano.gpc.dto.ProdutorRequestDTO;
import br.edu.ifbaiano.gpc.dto.ProdutorResponseDTO;
import br.edu.ifbaiano.gpc.dto.SafraResponseDTO;
import br.edu.ifbaiano.gpc.dto.TalhaoResponseDTO;
import br.edu.ifbaiano.gpc.service.ProdutorService;
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

/** RF01 e RF11 (safras por produtor). Recebe e devolve apenas DTOs. */
@RestController
@RequestMapping("/produtores")
public class ProdutorController {

    private final ProdutorService produtorService;
    private final TalhaoService talhaoService;
    private final SafraService safraService;

    public ProdutorController(ProdutorService produtorService, TalhaoService talhaoService,
                              SafraService safraService) {
        this.produtorService = produtorService;
        this.talhaoService = talhaoService;
        this.safraService = safraService;
    }

    @GetMapping
    public List<ProdutorResponseDTO> listar() {
        return produtorService.listar();
    }

    @GetMapping("/{id}")
    public ProdutorResponseDTO buscar(@PathVariable Long id) {
        return produtorService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ProdutorResponseDTO> criar(@Valid @RequestBody ProdutorRequestDTO dto) {
        ProdutorResponseDTO criado = produtorService.criar(dto);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public ProdutorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ProdutorRequestDTO dto) {
        return produtorService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/talhoes")
    public List<TalhaoResponseDTO> talhoes(@PathVariable Long id) {
        return talhaoService.listarPorProdutor(id);
    }

    @GetMapping("/{id}/safras")
    public List<SafraResponseDTO> safras(@PathVariable Long id) {
        return safraService.listarPorProdutor(id);
    }
}
