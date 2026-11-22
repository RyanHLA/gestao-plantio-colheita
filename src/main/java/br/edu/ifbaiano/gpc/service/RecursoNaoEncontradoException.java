package br.edu.ifbaiano.gpc.service;

/** Recurso inexistente. O Front Controller converte em HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " não encontrado(a)");
    }
}
