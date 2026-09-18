package com.caco.sitedocaco.modules.users.security;

import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.shared.entity.Role;
import com.caco.sitedocaco.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        persistUser(oAuth2User);
        return oAuth2User;
    }

    @Transactional
    void persistUser(OAuth2User oAuth2User) throws OAuth2AuthenticationException {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        // REGRA DE NEGÓCIO: Validação de Domínio
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "missing_email",
                    "O Google não retornou um e-mail.",
                    null
            ));
        }

        if (!email.endsWith("@dac.unicamp.br")) {
            OAuth2Error oauth2Error = new OAuth2Error(
                    "invalid_domain",
                    "O e-mail não pertence ao domínio institucional permitido (@dac.unicamp.br).",
                    null
            );

            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        // Salva ou atualiza o usuário
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Atualiza se existir, senão cria novo
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setAvatarUrl(avatarUrl);
            user.setRole(Role.STUDENT); // Padrão
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

    }
}
