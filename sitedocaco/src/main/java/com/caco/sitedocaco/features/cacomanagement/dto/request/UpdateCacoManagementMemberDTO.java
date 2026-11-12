package com.caco.sitedocaco.features.cacomanagement.dto.request;

import java.util.UUID;

public record UpdateCacoManagementMemberDTO(
        String name,
        UUID roleId
) {}
