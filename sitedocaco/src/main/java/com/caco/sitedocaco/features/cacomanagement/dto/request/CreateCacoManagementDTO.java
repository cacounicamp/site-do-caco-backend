package com.caco.sitedocaco.features.cacomanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateCacoManagementDTO(

    @NotBlank(message="O nome da gestão é obrigatório.")
    String name,

    @NotNull(message="A data de inicio é obrigatória.")
    LocalDate startDate,

    LocalDate endDate
){}
