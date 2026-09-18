package com.caco.sitedocaco.modules.home.dto.request;

import com.caco.sitedocaco.shared.entity.SeverityLevel;

import java.time.LocalDateTime;

public record UpdateWarningDTO(
        String markdownText,
        SeverityLevel severityLevel,
        LocalDateTime startsAt,
        LocalDateTime expiresAt
) {}
