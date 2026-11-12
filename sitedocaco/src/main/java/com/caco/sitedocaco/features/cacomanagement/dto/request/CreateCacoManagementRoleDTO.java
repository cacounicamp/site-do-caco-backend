package com.caco.sitedocaco.features.cacomanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCacoManagementRoleDTO(
    @NotBlank(message = "O nome do cargo é obrigatório.")
    String name
) {
}