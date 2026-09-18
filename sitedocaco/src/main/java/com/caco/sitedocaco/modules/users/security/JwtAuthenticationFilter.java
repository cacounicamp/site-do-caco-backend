package com.caco.sitedocaco.modules.users.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // 1. Tenta extrair o email.
        // Se o token for inválido ou expirado, isso retornará NULL (graças ao seu try-catch no JwtService).
        userEmail = jwtService.extractUsername(jwt);

        // 2. Se userEmail != null, o token é criptograficamente válido.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. Carrega o usuário do banco (para pegar as Roles e verificar suspensão)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. Rejeita contas suspensas (enabled=false) — retorna 403
            if (!userDetails.isEnabled()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"account_suspended\",\"message\":\"Sua conta foi suspensa. Entre em contato com o suporte.\"}");
                return;
            }

            // 5. Autentica direto, pois já sabemos que o token é válido pelo passo 1
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}