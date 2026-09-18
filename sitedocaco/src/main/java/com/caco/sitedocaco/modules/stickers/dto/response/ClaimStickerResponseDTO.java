package com.caco.sitedocaco.modules.stickers.dto.response;

import java.time.LocalDateTime;

public record ClaimStickerResponseDTO(
        StickerPublicDTO sticker,
        LocalDateTime obtainedAt
) {
}

