package com.caco.sitedocaco.modules.store.dto.response;

import java.util.UUID;

public record ProductImageResponseDTO(
        UUID id,
        String imageUrl,
        Integer displayOrder,
        UUID productId
) {}