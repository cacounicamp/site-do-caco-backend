package com.caco.sitedocaco.modules.exams.dto.response;

import com.caco.sitedocaco.modules.exams.entity.ExamType;

import java.util.UUID;

public record ExamWithoutSubjectDTO(
        UUID id,
        Integer year,
        ExamType type,
        String fileUrl
) {
}
