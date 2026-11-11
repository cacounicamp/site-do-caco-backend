package com.caco.sitedocaco.shared.config;

import com.caco.sitedocaco.features.users.security.FormRequiredFilter;
import com.caco.sitedocaco.features.users.security.JwtAuthenticationFilter;
import com.caco.sitedocaco.features.users.security.CustomOAuth2UserService;
import com.caco.sitedocaco.features.users.security.CustomOidcUserService;
import com.caco.sitedocaco.features.users.security.OAuth2LoginFailureHandler;
import com.caco.sitedocaco.features.users.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize nos controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FormRequiredFilter formRequiredFilter;

    // Frontend URL from application.properties:
    @Value("${app.frontend.url}")
    private String frontendUrl;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // 1. Desabilita CSRF (API Stateless não precisa)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configura CORS (Para o React acessar)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Define Sessão como Stateless (Não guarda JSESSIONID)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Regras de Autorização de URL
                .authorizeHttpRequests(auth -> auth
                        // Rotas Públicas
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Docs

                        // Rotas Admin (Super Admin também tem acesso)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Rotas Editor (também acessíveis por Admin e Super Admin)
                        .requestMatchers("/api/editor/**").hasAnyRole("EDITOR", "ADMIN", "SUPER_ADMIN")

                        // Rotas Super Admin
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                // 5. Configuração do Login com Google (OAuth2)
                .oauth2Login(oauth2 -> oauth2
                        // Serviço que processa o usuário vindo do Google
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        // Handler executado após login com sucesso (Gera JWT e redireciona)
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )

                // 6. Adiciona o filtro JWT antes do filtro padrão do Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 7. Middleware que exige o formulário de perfil preenchido para rotas protegidas
                .addFilterAfter(formRequiredFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
