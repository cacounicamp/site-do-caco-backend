package com.caco.sitedocaco.modules.stickers.dto.response;

import com.caco.sitedocaco.modules.events.dto.response.EventSummaryDTO;
import com.caco.sitedocaco.modules.stickers.entity.Sticker;

import java.time.LocalDateTime;
import java.util.UUID;

public record StickerAdminDTO(
        UUID id,
        String name,
        String description,
        String imageUrl,
        EventSummaryDTO originEvent,
        LocalDateTime createdAt
) {
    public static StickerAdminDTO fromEntity(Sticker sticker) {
        return new StickerAdminDTO(
                sticker.getId(),
                sticker.getName(),
                sticker.getDescription(),
                sticker.getImageUrl(),
                sticker.getOriginEvent() != null ? EventSummaryDTO.fromEntity(sticker.getOriginEvent()) : null,
                sticker.getCreatedAt()
        );
    }
}

