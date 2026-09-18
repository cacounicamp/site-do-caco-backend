package com.caco.sitedocaco.modules.events.dto.request;

import com.caco.sitedocaco.modules.events.entity.Event;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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