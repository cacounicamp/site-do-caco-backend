package com.caco.sitedocaco.modules.users.service;

import com.caco.sitedocaco.modules.users.dto.request.UserProfileFormRequest;
import com.caco.sitedocaco.modules.users.dto.response.UserProfileResponse;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.users.entity.UserProfile;
import com.caco.sitedocaco.shared.entity.CourseType;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.users.repository.UserProfileRepository;
import com.caco.sitedocaco.shared.contract.UserProfileAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService implements UserProfileAccess {

    private static final int MIN_ENTRY_YEAR = 2018;
    private static final int UNKNOWN_ENTRY_YEAR = -1;

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;

    /**
     * Submete o formulário de perfil. Só pode ser feito uma vez – respostas não podem ser editadas.
     */
    @Transactional
    public UserProfileResponse submitForm(UserProfileFormRequest request) {
        User currentUser = getCurrentUser();

        if (userProfileRepository.existsByUser(currentUser)) {
            throw new BusinessRuleException("O formulário já foi respondido e não pode ser editado.");
        }

        // Validação: se curso == OUTRO, otherCourseName é obrigatório
        if (request.course() == CourseType.OUTRO) {
            if (request.otherCourseName() == null || request.otherCourseName().isBlank()) {
                throw new BusinessRuleException("Quando o curso é 'Outro', o nome do curso deve ser informado.");
            }
        }

        int currentYear = LocalDate.now().getYear();
        int resolvedYear = resolveEntryYear(request.entryYear(), currentYear);

        UserProfile profile = new UserProfile();
        profile.setUser(currentUser);
        profile.setCourse(request.course());
        profile.setOtherCourseName(request.course() == CourseType.OUTRO ? request.otherCourseName() : null);
        profile.setEntryYear(resolvedYear);

        return UserProfileResponse.fromEntity(userProfileRepository.save(profile));
    }

    /**
     * Retorna o formulário respondido do usuário logado.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();
        UserProfile profile = userProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário ainda não respondido."));
        return UserProfileResponse.fromEntity(profile);
    }

    /**
     * Retorna o perfil do usuário logado como Optional (sem lançar exceção).
     * Usado internamente para enriquecer outros DTOs.
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<UserProfile> findMyProfile() {
        User currentUser = getCurrentUser();
        return userProfileRepository.findByUser(currentUser);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    /**
     * Regras do ano de ingresso:
     * - < MIN_ENTRY_YEAR  → salva como -1
     * - > currentYear     → rejeita com exceção
     * - caso contrário    → usa o valor enviado
     */
    private int resolveEntryYear(int submitted, int currentYear) {
        if (submitted > currentYear) {
            throw new BusinessRuleException(
                    "O ano de ingresso não pode ser maior que o ano atual (" + currentYear + ").");
        }
        if (submitted < MIN_ENTRY_YEAR) {
            return UNKNOWN_ENTRY_YEAR;
        }
        return submitted;
    }
}
