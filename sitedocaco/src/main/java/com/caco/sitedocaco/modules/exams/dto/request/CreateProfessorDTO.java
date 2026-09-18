package com.caco.sitedocaco.modules.exams.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProfessorDTO(
        @NotBlank(message = "O nome do professor é obrigatório")
        String name
) {}

