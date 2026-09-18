package com.caco.sitedocaco.modules.exams.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSubjectDTO(
        @NotBlank(message = "O código da disciplina é obrigatório")
        String subjectCode,

        @NotBlank(message = "O nome da disciplina é obrigatório")
        String name
) {}