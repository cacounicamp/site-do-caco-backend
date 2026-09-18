package com.caco.sitedocaco.modules.events.dto.response;

import com.caco.sitedocaco.modules.events.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventAdminResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String coverImage,
        Event.EventType type,
        Event.EventImportance importance,
        Event.EventStatus status,
        List<EventGalleryItemDTO> gallery,
        EventParticipationStatsDTO participationStats
) {}