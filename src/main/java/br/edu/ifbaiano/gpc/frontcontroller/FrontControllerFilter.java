package br.edu.ifbaiano.gpc.frontcontroller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Padrão <b>Front Controller</b> — pré-processamento centralizado.
 *
 * <p>No Spring MVC, o ponto único de entrada de todas as requisições HTTP é o
 * {@code DispatcherServlet}. Este filtro roda antes dele, uma vez por requisição,
 * e concentra as tarefas transversais que nenhum controller precisa repetir:
 * <ol>
 *   <li>gera um identificador de rastreio ({@code X-Request-Id});</li>
 *   <li>autentica as operações de escrita (POST, PUT, PATCH, DELETE) pela chave
 *       enviada no cabeçalho {@code X-API-Key};</li>
 *   <li>registra no log o método, o caminho, o status e o tempo de resposta.</li>
 * </ol>
 * O tratamento de exceções, também centralizado, fica em {@link TratadorGlobalExcecoes}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FrontControllerFilter extends OncePerRequestFilter {

    public static final String CABECALHO_API_KEY = "X-API-Key";
    public static final String CABECALHO_REQUEST_ID = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(FrontControllerFilter.class);
    private static final Set<String> METODOS_ESCRITA = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name(), HttpMethod.DELETE.name());

    private final String apiKey;

    public FrontControllerFilter(@Value("${gpc.seguranca.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    /** O console do H2 é uma ferramenta de desenvolvimento, fora da API. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        response.setHeader(CABECALHO_REQUEST_ID, requestId);
        MDC.put("requestId", requestId);
        try {
            if (exigeAutenticacao(request) && !apiKey.equals(request.getHeader(CABECALHO_API_KEY))) {
                negarAcesso(request, response);
                return;
            }
            chain.doFilter(request, response); // despacha para o DispatcherServlet -> controller
        } finally {
            log.info("[{}] {} {} -> {} ({} ms)", requestId, request.getMethod(), request.getRequestURI(),
                    response.getStatus(), System.currentTimeMillis() - inicio);
            MDC.remove("requestId");
        }
    }

    private boolean exigeAutenticacao(HttpServletRequest request) {
        return !apiKey.isBlank() && METODOS_ESCRITA.contains(request.getMethod());
    }

    private static void negarAcesso(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"timestamp":"%s","status":401,"erro":"Unauthorized",\
                "mensagem":"Informe uma chave válida no cabeçalho %s para operações de escrita",\
                "caminho":"%s","campos":null}"""
                .formatted(OffsetDateTime.now(), CABECALHO_API_KEY, request.getRequestURI().replace("\"", "")));
    }
}
