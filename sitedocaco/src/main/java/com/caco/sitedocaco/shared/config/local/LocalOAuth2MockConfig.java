package com.caco.sitedocaco.shared.config.local;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
@Configuration
@Profile("local")
public class LocalOAuth2MockConfig {

    @Value("${server.port:8080}")
    private int appPort;

    @Value("${wiremock.oauth2.port:9988}")
    private int wireMockPort;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockOAuth2Server() {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.options()
                        .port(wireMockPort)
                        .globalTemplating(true)
        );

        String issuer = "http://localhost:" + wireMockPort;
        // O callback OAuth2 fica FORA do context-path (/api).
        String callback = "http://localhost:" + appPort + "/login/oauth2/code/google";

        // Discovery (não é obrigatório, mas útil para depuração)
        server.stubFor(get(urlEqualTo("/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                        "issuer": "%s",
                        "authorization_endpoint": "%s/oauth2/authorize",
                        "token_endpoint": "%s/oauth2/token",
                        "userinfo_endpoint": "%s/userinfo",
                        "jwks_uri": "%s/.well-known/jwks.json",
                        "response_types_supported": ["code"],
                        "subject_types_supported": ["public"],
                        "id_token_signing_alg_values_supported": ["RS256"]
                    }
                    """.formatted(issuer, issuer, issuer, issuer, issuer))));

        // Authorization endpoint -> redirect para o callback do Spring Security
        server.stubFor(get(urlPathEqualTo("/oauth2/authorize"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", callback + "?code=fake-code&state=test-state")));

        // Token endpoint
        server.stubFor(post(urlPathEqualTo("/oauth2/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                        "access_token": "fake-access-token",
                        "token_type": "Bearer",
                        "expires_in": 3600,
                        "refresh_token": "fake-refresh-token",
                        "scope": "profile email"
                    }
                    """)));

        // Userinfo endpoint
        server.stubFor(get(urlPathEqualTo("/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                        "sub": "1234567890",
                        "name": "Local User",
                        "given_name": "Local",
                        "family_name": "User",
                        "email": "user@dac.unicamp.br",
                        "email_verified": true,
                        "picture": "https://via.placeholder.com/150"
                    }
                    """)));

        log.info("🎭 WireMock OAuth2 ouvindo em http://localhost:{}", wireMockPort);
        return server;
    }
}