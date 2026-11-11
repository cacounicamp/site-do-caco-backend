package com.caco.sitedocaco.features.events.dto.request;

import com.caco.sitedocaco.features.events.entity.Event;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public record UpdateEventDTO(
        String title,
        String slug,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String locationUrl,
        MultipartFile coverImage,
        Boolean removeCoverImage,
        Event.EventType type,
        Event.EventImportance importance,
        Event.EventStatus status
) {}