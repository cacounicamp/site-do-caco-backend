package com.caco.sitedocaco.features.cacomanagement.dto.request;

import java.time.LocalDate;

public record UpdateCacoManagementDTO(
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}
