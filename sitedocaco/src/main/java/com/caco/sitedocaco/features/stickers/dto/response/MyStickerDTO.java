package com.caco.sitedocaco.features.stickers.dto.response;

import com.caco.sitedocaco.features.stickers.entity.UserSticker;

import java.time.LocalDateTime;

public record MyStickerDTO(
        StickerPublicDTO sticker,
        LocalDateTime obtainedAt
) {
    public static MyStickerDTO fromEntity(UserSticker userSticker) {
        return new MyStickerDTO(
                StickerPublicDTO.fromEntity(userSticker.getSticker()),
                userSticker.getObtainedAt()
        );
    }
}

