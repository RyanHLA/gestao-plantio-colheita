package br.edu.ifbaiano.gpc.service;

/** Violação de regra de negócio (ex.: CPF duplicado). O Front Controller converte em HTTP 409. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
