package com.caco.sitedocaco.features.users.service;

import com.caco.sitedocaco.features.users.dto.request.UpdateProfileDTO;
import com.caco.sitedocaco.features.users.dto.response.UserResponseDTO;
import com.caco.sitedocaco.features.users.entity.User;
import com.caco.sitedocaco.features.whatsapp.service.WhatsAppGroupService;
import com.caco.sitedocaco.shared.entity.Role;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.features.users.repository.UserRepository;
import com.caco.sitedocaco.shared.storage.FileStorage;
import com.caco.sitedocaco.shared.storage.UploadRequest;
import com.caco.sitedocaco.shared.storage.kind.ImageKind;
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
public class UserService {

    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    @Lazy
    @Autowired
    private WhatsAppGroupService whatsAppGroupService;

    /**
     * Pega o e-mail do Token JWT (via SecurityContext) e busca o usuário no banco.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado no banco."));

        String whatsappLink = whatsAppGroupService.getWhatsAppLinkForCurrentUser();
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
            return fileStorage.store(UploadRequest.of(avatarFile, ImageKind.PROFILE_AVATAR)).url();
        } catch (IOException e) {
            throw new IOException("Não foi possível fazer upload da imagem do perfil", e);
        }
    }

    @Transactional(readOnly = true)
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
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
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
