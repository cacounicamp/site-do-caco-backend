package com.caco.sitedocaco.modules.users.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.caco.sitedocaco.modules.users.entity.User;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 1. Gerar Token (Login bem sucedido)
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail()) // Identificador principal
                .withClaim("id", user.getId().toString()) // Payload útil
                .withClaim("role", user.getRole().name()) // Payload útil para o Front
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(expiration, ChronoUnit.MILLIS))
                .withIssuer("caco-api")
                .sign(Algorithm.HMAC256(secret));
    }

    // 2. Validar e Decodificar Token
    public DecodedJWT validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("caco-api")
                    .build()
                    .verify(token);
        } catch (Exception e) {
            // Token inválido ou expirado
            return null;
        }
    }

    // 3. Extrair E-mail (Subject)
    public String extractUsername(String token) {
        DecodedJWT jwt = validateToken(token);
        return jwt != null ? jwt.getSubject() : null;
    }

    // 4. Expiration Time in Millis
    public long getExpirationInMillis() {
        return expiration;
    }
}