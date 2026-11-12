package com.caco.sitedocaco.features.cacomanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCacoManagementMemberDTO(
    @NotNull
    UUID cacoManagementId,

    @NotBlank(message = "O nome é obrigatório")
    String name,

    @NotNull
    UUID roleId
) {}
