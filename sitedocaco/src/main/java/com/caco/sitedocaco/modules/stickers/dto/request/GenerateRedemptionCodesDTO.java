package com.caco.sitedocaco.modules.stickers.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record GenerateRedemptionCodesDTO(
        @Min(value = 1, message = "quantity deve ser >= 1")
        @Max(value = 500, message = "quantity deve ser <= 500")
        int quantity,
        Boolean oneTimeUse,
        LocalDateTime expiresAt
) {
}

