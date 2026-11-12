package com.caco.sitedocaco.features.cacomanagement.dto.response;

import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagementRole;

import java.util.UUID;

public record CacoManagementRoleResponseDTO(
        UUID id,
        String name
) {
    public static CacoManagementRoleResponseDTO fromEntity(CacoManagementRole role) {
        return new CacoManagementRoleResponseDTO(
                role.getId(),
                role.getRoleName()
        );
    }
}
