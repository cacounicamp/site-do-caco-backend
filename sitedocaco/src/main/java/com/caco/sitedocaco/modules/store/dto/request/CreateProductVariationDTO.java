package com.caco.sitedocaco.modules.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateProductVariationDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @PositiveOrZero(message = "O preço adicional deve ser zero ou positivo")
        BigDecimal additionalPrice,

        @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
        Integer stockQuantity
) {}