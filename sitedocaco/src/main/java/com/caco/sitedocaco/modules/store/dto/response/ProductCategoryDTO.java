package com.caco.sitedocaco.modules.store.dto.response;

import java.util.UUID;

public record ProductCategoryDTO(
        UUID id,
        String name,
        String slug,
        Integer order
) {}
