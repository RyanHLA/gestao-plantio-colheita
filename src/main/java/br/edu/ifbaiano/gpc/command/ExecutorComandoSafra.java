package br.edu.ifbaiano.gpc.command;

import br.edu.ifbaiano.gpc.dto.HistoricoComandoDTO;
import br.edu.ifbaiano.gpc.service.RecursoNaoEncontradoException;
import br.edu.ifbaiano.gpc.model.Safra;
import br.edu.ifbaiano.gpc.repository.SafraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Invocador do padrão Command. Não conhece as regras de cada operação:
 * só carrega a safra, executa o comando dentro de uma transação e guarda
 * o histórico das execuções (auditoria).
 */
@Component
public class ExecutorComandoSafra {

    private static final Logger log = LoggerFactory.getLogger(ExecutorComandoSafra.class);
    private static final int TAMANHO_HISTORICO = 100;

    private final SafraRepository safraRepository;
    private final Deque<HistoricoComandoDTO> historico = new ConcurrentLinkedDeque<>();

    public ExecutorComandoSafra(SafraRepository safraRepository) {
        this.safraRepository = safraRepository;
    }

    @Transactional
    public <R> R executar(Long safraId, ComandoSafra<R> comando) {
        Safra safra = safraRepository.findById(safraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Safra", safraId));
        try {
            R resultado = comando.executar(safra);
            registrar(safraId, comando, true, "OK");
            return resultado;
        } catch (RuntimeException e) {
            registrar(safraId, comando, false, e.getMessage());
            throw e;
        }
    }

    /** Execuções mais recentes primeiro. */
    public List<HistoricoComandoDTO> historico() {
        return new ArrayList<>(historico);
    }

    private void registrar(Long safraId, ComandoSafra<?> comando, boolean sucesso, String mensagem) {
        historico.addFirst(new HistoricoComandoDTO(OffsetDateTime.now(), safraId, comando.nome(), sucesso, mensagem));
        while (historico.size() > TAMANHO_HISTORICO) {
            historico.pollLast();
        }
        log.info("Comando {} na safra {}: {}", comando.nome(), safraId, sucesso ? "sucesso" : "falha - " + mensagem);
    }
}
