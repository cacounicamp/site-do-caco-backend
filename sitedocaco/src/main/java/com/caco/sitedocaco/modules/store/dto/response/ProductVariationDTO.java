package com.caco.sitedocaco.modules.store.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariationDTO(
        UUID id,
        String name,
        BigDecimal additionalPrice,
        boolean available // se manageStock=false ou stockQuantity>0
) {}

