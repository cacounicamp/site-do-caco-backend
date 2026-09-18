package com.caco.sitedocaco.modules.events.dto.request;

import com.caco.sitedocaco.modules.events.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public record CreateEventDTO(
        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotBlank(message = "O slug é obrigatório")
        String slug,

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @NotNull(message = "A data de início é obrigatória")
        LocalDateTime startDate,

        @NotNull(message = "A data de término é obrigatória")
        LocalDateTime endDate,

        String location,
        String locationUrl,
        MultipartFile coverImage,

        @NotNull(message = "O tipo é obrigatório")
        Event.EventType type,

        @NotNull(message = "A importância é obrigatória")
        Event.EventImportance importance
) {}