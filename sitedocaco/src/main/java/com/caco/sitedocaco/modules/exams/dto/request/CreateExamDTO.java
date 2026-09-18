// main/java/com.caco.sitedocaco.modules.exams.dto.request.CreateExamDTO.java
package com.caco.sitedocaco.modules.exams.dto.request;

import com.caco.sitedocaco.modules.exams.entity.ExamType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateExamDTO(
        @NotNull(message = "A disciplina é obrigatória")
        String subjectCode,

        UUID professorId,

        @NotNull(message = "O ano é obrigatório")
        Integer year,

        @NotNull(message = "O tipo de prova é obrigatório")
        ExamType type,

        @NotNull(message = "O link do PDF é obrigatório")
        String fileUrl
) {}