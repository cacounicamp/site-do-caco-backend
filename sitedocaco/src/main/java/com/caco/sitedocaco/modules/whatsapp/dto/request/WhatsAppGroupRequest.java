package com.caco.sitedocaco.modules.whatsapp.dto.request;

import com.caco.sitedocaco.shared.entity.CourseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WhatsAppGroupRequest(
        @NotNull CourseType course,
        @Positive int entryYear,
        @NotBlank String whatsappLink
) {}

