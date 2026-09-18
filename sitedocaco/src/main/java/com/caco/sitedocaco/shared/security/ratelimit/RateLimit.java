package com.caco.sitedocaco.shared.security.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Aplica rate limiting ao endpoint anotado.
 *
 * <p>O limite é por IP para requisições anônimas e por usuário (email) para requisições autenticadas.
 *
 * <p>Exemplo: {@code @RateLimit(capacity = 10, refillTokens = 10, refillPeriod = 1, unit = TimeUnit.MINUTES)}
 * permite 10 requisições por minuto, com recarga completa a cada minuto.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** Capacidade máxima do balde (burst máximo). */
    long capacity() default 60;

    /** Quantidade de tokens adicionados a cada período. */
    long refillTokens() default 60;

    /** Duração do período de recarga. */
    long refillPeriod() default 1;

    /** Unidade de tempo do período. */
    TimeUnit unit() default TimeUnit.MINUTES;
}

