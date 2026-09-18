package com.caco.sitedocaco.modules.home.dto.request;

import com.caco.sitedocaco.shared.entity.SeverityLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateWarningDTO(
        @NotBlank String markdownText,
        SeverityLevel severityLevel,
        @NotNull LocalDateTime startsAt,
        @NotNull @Future LocalDateTime expiresAt
) {}
