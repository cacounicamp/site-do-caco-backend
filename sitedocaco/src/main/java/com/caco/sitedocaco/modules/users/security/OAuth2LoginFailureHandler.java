package com.caco.sitedocaco.modules.users.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // Pega a mensagem de erro da exceção (ou define um código padrão)
        // Se a exceção for "invalid_domain", mandamos esse código.
        String errorCode = "generic_error";

        // 1. Verifica se a exceção é do tipo OAuth2 (que carrega nosso código personalizado)
        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            OAuth2Error error = oauthEx.getError();

            // 2. Lê o código que definimos no Service ("invalid_domain")
            if (error != null) {
                errorCode = error.getErrorCode();
            }
        }

        // Monta a URL: http://localhost:5173/login?error=invalid_domain
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                .queryParam("error", errorCode)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}