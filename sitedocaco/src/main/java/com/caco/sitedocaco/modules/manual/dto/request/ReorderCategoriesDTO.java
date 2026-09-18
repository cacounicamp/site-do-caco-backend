package com.caco.sitedocaco.modules.manual.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderCategoriesDTO(
        @NotNull
        List<UUID> categoryIds
) {}