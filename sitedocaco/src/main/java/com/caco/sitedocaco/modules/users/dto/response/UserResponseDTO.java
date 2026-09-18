package com.caco.sitedocaco.modules.users.dto.response;

import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.shared.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        Role role,
        boolean suspended,
        LocalDateTime createdAt,
        String whatsappGroupLink
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isSuspended(),
                user.getCreatedAt(),
                null
        );
    }

    public static UserResponseDTO fromEntity(User user, String whatsappGroupLink) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isSuspended(),
                user.getCreatedAt(),
                whatsappGroupLink
        );
    }
}



