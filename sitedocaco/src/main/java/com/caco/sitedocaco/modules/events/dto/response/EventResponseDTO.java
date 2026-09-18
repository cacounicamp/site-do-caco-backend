package com.caco.sitedocaco.modules.events.dto.response;

import com.caco.sitedocaco.modules.events.entity.Event;
import com.caco.sitedocaco.modules.events.entity.UserEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String title,
        String slug,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String locationUrl,
        String coverImage,
        Event.EventType type,
        Event.EventImportance importance,
        Event.EventStatus status,
        List<EventGalleryItemDTO> gallery,
        UserEvent.ParticipationStatus userParticipationStatus
) {
    public static EventResponseDTO fromEntity(
            Event event,
            List<EventGalleryItemDTO> gallery,
            UserEvent.ParticipationStatus userStatus
    ) {
        return new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getSlug(),
                event.getDescription(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLocation(),
                event.getLocationUrl(),
                event.getCoverImage(),
                event.getType(),
                event.getImportance(),
                event.getStatus(),
                gallery,
                userStatus
        );
    }
}