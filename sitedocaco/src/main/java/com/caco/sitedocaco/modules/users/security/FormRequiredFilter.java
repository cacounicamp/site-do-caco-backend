package com.caco.sitedocaco.modules.users.security;

import com.caco.sitedocaco.modules.users.repository.UserProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Middleware que verifica se o usuário autenticado já preencheu o formulário de perfil.
 * Caso não tenha preenchido, retorna 403 com uma mensagem explicativa.
 *
 * <p>Rotas isentas (bypass):
 * <ul>
 *   <li>Qualquer rota pública  (/api/public/**)</li>
 *   <li>O próprio endpoint do formulário (/api/user/profile-form) – para que o usuário consiga respondê-lo</li>
 *   <li>Rotas OAuth2 e Swagger</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class FormRequiredFilter extends OncePerRequestFilter {

    private final UserProfileRepository userProfileRepository;

    /**
     * Prefixos/caminhos exatos que não exigem o formulário preenchido.
     */
    private static final Set<String> BYPASS_PREFIXES = Set.of(
            //"/api/public/",
            "/api/user/profile-form",
            "/oauth2/",
            "/login/"
            //"/swagger-ui",
            //"/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Apenas verifica usuários autenticados (com principal real)
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Pula rotas que não precisam do formulário
        if (shouldBypass(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = auth.getName();

        if (!userProfileRepository.existsByUserEmail(email)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"form_required\"," +
                    "\"message\":\"Você precisa responder o formulário de perfil antes de continuar. " +
                    "Acesse POST /api/user/profile-form para preenchê-lo.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldBypass(String path) {
        for (String prefix : BYPASS_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

