package com.caco.sitedocaco.modules.home.dto.response;

import com.caco.sitedocaco.shared.entity.SeverityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record WarningDTO(
        UUID id,
        String markdownText,
        SeverityLevel severityLevel,
        LocalDateTime startsAt,
        LocalDateTime expiresAt
) {}