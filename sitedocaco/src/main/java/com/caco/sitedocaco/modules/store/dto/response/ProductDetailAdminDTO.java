package com.caco.sitedocaco.modules.store.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductDetailAdminDTO(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        BigDecimal originalPrice,
        boolean manageStock,
        Integer stockQuantity,
        boolean active,
        UUID categoryId,
        String categoryName,
        String categorySlug,
        List<String> images,
        List<ProductVariationDTO> variations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}