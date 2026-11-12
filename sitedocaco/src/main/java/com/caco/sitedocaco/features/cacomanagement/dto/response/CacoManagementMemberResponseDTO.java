package com.caco.sitedocaco.features.cacomanagement.dto.response;

import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagementMember;

import java.util.UUID;

public record CacoManagementMemberResponseDTO(
        UUID id,
        UUID cacoManagementId,
        String name,
        UUID roleId,
        String roleName
) {
    public static CacoManagementMemberResponseDTO fromEntity(CacoManagementMember member) {
        return new CacoManagementMemberResponseDTO(
                member.getId(),
                member.getCacoManagement().getId(),
                member.getMemberName(),
                member.getMemberRole().getId(),
                member.getMemberRole().getRoleName()
        );
    }
}
