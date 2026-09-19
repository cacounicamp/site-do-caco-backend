package com.caco.sitedocaco.features.stickers.dto.response;

import java.time.LocalDateTime;

public record ClaimStickerResponseDTO(
        StickerPublicDTO sticker,
        LocalDateTime obtainedAt
) {
}

