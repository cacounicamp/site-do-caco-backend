package com.caco.sitedocaco.modules.users.dto.request;

import com.caco.sitedocaco.shared.entity.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleDTO(
        @NotNull(message = "O role não pode ser nulo")
        Role role
) {}

