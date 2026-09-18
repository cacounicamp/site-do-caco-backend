package com.caco.sitedocaco.modules.home.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderBannersDTO(
        @NotNull List<UUID> bannerIds // Lista ordenada de IDs ex: [ID_A, ID_D, ID_B...]
) {}
