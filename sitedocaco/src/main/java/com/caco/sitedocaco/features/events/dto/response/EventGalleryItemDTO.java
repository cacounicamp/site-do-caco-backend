package com.caco.sitedocaco.features.events.dto.response;

import com.caco.sitedocaco.features.events.entity.EventGalleryItem;

import java.util.UUID;

public record EventGalleryItemDTO(
        UUID id,
        String mediaUrl,
        EventGalleryItem.MediaType type,
        String caption
) {
    public static EventGalleryItemDTO fromEntity(EventGalleryItem item) {
        return new EventGalleryItemDTO(
                item.getId(),
                item.getMediaUrl(),
                item.getType(),
                item.getCaption()
        );
    }
}