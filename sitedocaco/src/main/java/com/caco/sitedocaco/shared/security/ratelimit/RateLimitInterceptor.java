package com.caco.sitedocaco.shared.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor que aplica rate limiting por endpoint.
 *
 * <p>Funciona lendo a anotação {@link RateLimit} no método ou na classe do controller.
 * A chave do balde é composta por: {@code [classe#método]-[usuário ou IP]},
 * garantindo isolamento por endpoint e por cliente.
 *
 * <p>Se nenhuma anotação for encontrada, a requisição passa sem limitação.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Mapa global de buckets: chave = "ENDPOINT_KEY:CLIENT_KEY"
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = resolveAnnotation(handlerMethod);
        if (rateLimit == null) {
            return true;
        }

        String bucketKey = buildBucketKey(request, handlerMethod);
        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> buildBucket(rateLimit));

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"error\":\"rate_limit_exceeded\"," +
                "\"message\":\"Muitas requisições. Tente novamente em instantes.\"}"
        );
        return false;
    }

    /**
     * Resolve a anotação {@link RateLimit}: primeiro no método, depois na classe.
     * Isso permite que anotações de método sobrescrevam a do controller.
     */
    private RateLimit resolveAnnotation(HandlerMethod handlerMethod) {
        RateLimit methodLevel = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (methodLevel != null) return methodLevel;
        return handlerMethod.getBeanType().getAnnotation(RateLimit.class);
    }

    /**
     * Constrói a chave única do balde: endpoint + identidade do cliente.
     * Para usuários autenticados usa o email; para anônimos usa o IP.
     */
    private String buildBucketKey(HttpServletRequest request, HandlerMethod handlerMethod) {
        String endpointKey = handlerMethod.getBeanType().getSimpleName()
                + "#" + handlerMethod.getMethod().getName();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientKey;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            clientKey = "user:" + auth.getName();
        } else {
            clientKey = "ip:" + getClientIp(request);
        }

        return endpointKey + ":" + clientKey;
    }

    /**
     * Cria um bucket com a configuração da anotação usando a API do Bucket4j 8.x.
     */
    private Bucket buildBucket(RateLimit rateLimit) {
        Duration period = Duration.of(rateLimit.refillPeriod(), rateLimit.unit().toChronoUnit());
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(rateLimit.capacity())
                .refillGreedy(rateLimit.refillTokens(), period)
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * Extrai o IP real do cliente, respeitando proxies reversos (X-Forwarded-For).
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
