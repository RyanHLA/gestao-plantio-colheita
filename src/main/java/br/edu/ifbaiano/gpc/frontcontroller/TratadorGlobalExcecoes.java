package br.edu.ifbaiano.gpc.frontcontroller;

import br.edu.ifbaiano.gpc.dto.ErroDTO;
import br.edu.ifbaiano.gpc.service.RecursoNaoEncontradoException;
import br.edu.ifbaiano.gpc.service.RegraNegocioException;
import br.edu.ifbaiano.gpc.service.OpenMeteoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Padrão <b>Front Controller</b> — tratamento de exceções centralizado.
 *
 * <p>Todas as exceções lançadas pelos controllers, services e entidades passam por
 * aqui e viram uma resposta JSON padronizada ({@link ErroDTO}) com o status HTTP
 * adequado. Assim nenhum controller precisa de try/catch.
 */
@RestControllerAdvice
public class TratadorGlobalExcecoes {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalExcecoes.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroDTO> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada", req, null);
    }

    /** Regras de negócio e transições de status inválidas (ex.: colher safra não plantada). */
    @ExceptionHandler({RegraNegocioException.class, IllegalStateException.class})
    public ResponseEntity<ErroDTO> conflito(RuntimeException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroDTO> integridade(DataIntegrityViolationException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Operação viola uma restrição do banco de dados", req, null);
    }

    /** Validações do Bean Validation nos DTOs de entrada (@NotBlank, @Positive...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", req, campos);
    }

    /** Regras de validação das próprias entidades (ex.: temperatura mínima > máxima). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDTO> argumentoInvalido(IllegalArgumentException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "JSON inválido ou com valor em formato errado (datas: AAAA-MM-DD; enums em maiúsculas)", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDTO> parametroInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "': " + e.getValue(), req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroDTO> metodoNaoSuportado(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroDTO> tipoNaoSuportado(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie o corpo como application/json", req, null);
    }

    /** Falha ao consultar a API externa de clima. */
    @ExceptionHandler({OpenMeteoException.class, RestClientException.class})
    public ResponseEntity<ErroDTO> apiExterna(RuntimeException e, HttpServletRequest req) {
        log.warn("Falha na Open-Meteo: {}", e.getMessage());
        return resposta(HttpStatus.BAD_GATEWAY,
                "Não foi possível consultar a previsão do tempo (Open-Meteo). Tente novamente.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDTO> inesperado(Exception e, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", req, null);
    }

    private static ResponseEntity<ErroDTO> resposta(HttpStatus status, String mensagem,
                                                    HttpServletRequest req, Map<String, String> campos) {
        ErroDTO corpo = new ErroDTO(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
