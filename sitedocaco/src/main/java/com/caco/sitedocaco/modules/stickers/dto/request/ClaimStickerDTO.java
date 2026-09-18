package com.caco.sitedocaco.modules.stickers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimStickerDTO(
        @NotBlank(message = "code é obrigatório")
        @Size(min = 8, max = 12, message = "code deve ter entre 8 e 12 caracteres")
        String code
) {
}

