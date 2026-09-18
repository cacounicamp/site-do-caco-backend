package com.caco.sitedocaco.modules.users.service;

import com.caco.sitedocaco.modules.users.dto.request.UpdateProfileDTO;
import com.caco.sitedocaco.modules.users.dto.response.UserResponseDTO;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.shared.entity.Role;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import com.caco.sitedocaco.shared.contract.UserAccess;
import com.caco.sitedocaco.shared.contract.WhatsAppLinkProvider;
import com.caco.sitedocaco.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserAccess {

    private final UserRepository userRepository;
    private final ImgBBService imgBBService;

    @Lazy
    @Autowired
    private WhatsAppLinkProvider whatsAppLinkProvider;

    /**
     * Pega o e-mail do Token JWT (via SecurityContext) e busca o usuário no banco.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado no banco."));

        String whatsappLink = whatsAppLinkProvider.getWhatsAppLinkForCurrentUser();
        return UserResponseDTO.fromEntity(user, whatsappLink);
    }

    /**
     * Atualiza dados do perfil do usuário logado
     */
    @Transactional
    public UserResponseDTO updateProfile(UpdateProfileDTO request) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setUsername(request.name());
        }

        if (request.avatar() != null && !request.avatar().isEmpty()) {
            String newAvatarUrl = uploadAvatarImage(request.avatar());
            user.setAvatarUrl(newAvatarUrl);
        }

        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }

    private String uploadAvatarImage(MultipartFile avatarFile) throws IOException {
        try {
            return imgBBService.uploadImage(avatarFile, ImageType.PROFILE_AVATAR);
        } catch (IOException e) {
            throw new IOException("Não foi possível fazer upload da imagem do perfil", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public void removeAvatar() {
        User user = getCurrentUser();
        user.setAvatarUrl(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    // ── Super Admin methods ───────────────────────────────────────────────────

    /**
     * Lista todos os usuários com paginação (apenas Super Admin).
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponseDTO::fromEntity);
    }

    /**
     * Retorna as informações completas de um usuário pelo ID (apenas Super Admin).
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return UserResponseDTO.fromEntity(user);
    }

    /**
     * Promove ou demove um usuário para o Role informado (apenas Super Admin).
     * O Super Admin não pode alterar o próprio role nem promover alguém a SUPER_ADMIN.
     */
    @Transactional
    public UserResponseDTO changeUserRole(UUID userId, Role newRole) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (target.getEmail().equals(currentEmail)) {
            throw new BusinessRuleException("O Super Admin não pode alterar o próprio role.");
        }
        if (newRole == Role.SUPER_ADMIN) {
            throw new BusinessRuleException("Não é permitido promover usuários para Super Admin via API.");
        }

        target.setRole(newRole);
        return UserResponseDTO.fromEntity(userRepository.save(target));
    }

    /**
     * Suspende a conta de um usuário (apenas Super Admin).
     */
    @Transactional
    public UserResponseDTO suspendUser(UUID userId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (target.getEmail().equals(currentEmail)) {
            throw new BusinessRuleException("O Super Admin não pode suspender a própria conta.");
        }
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessRuleException("Não é possível suspender outro Super Admin.");
        }
        if (target.isSuspended()) {
            throw new BusinessRuleException("A conta deste usuário já está suspensa.");
        }

        target.setSuspended(true);
        return UserResponseDTO.fromEntity(userRepository.save(target));
    }

    /**
     * Libera/reativa a conta de um usuário suspenso (apenas Super Admin).
     */
    @Transactional
    public UserResponseDTO unsuspendUser(UUID userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!target.isSuspended()) {
            throw new BusinessRuleException("A conta deste usuário não está suspensa.");
        }

        target.setSuspended(false);
        return UserResponseDTO.fromEntity(userRepository.save(target));
    }
}
