package com.caco.sitedocaco.modules.store.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductOverviewDTO(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        String coverImage,
        boolean outOfStock,
        UUID categoryId,
        String categoryName,
        String categorySlug,
        LocalDateTime createdAt
) {}