package com.caco.sitedocaco.features.users.dto.request;

import com.caco.sitedocaco.shared.entity.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleDTO(
        @NotNull(message = "O role não pode ser nulo")
        Role role
) {}

