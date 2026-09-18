package com.caco.sitedocaco.modules.events.dto.response;

import com.caco.sitedocaco.modules.events.entity.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventSummaryDTO(
        UUID id,
        String title,
        String slug,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String coverImage,
        Event.EventType type,
        Event.EventImportance importance,
        Event.EventStatus status
) {
    public static EventSummaryDTO fromEntity(Event event) {
        return new EventSummaryDTO(
                event.getId(),
                event.getTitle(),
                event.getSlug(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLocation(),
                event.getCoverImage(),
                event.getType(),
                event.getImportance(),
                event.getStatus()
        );
    }
}