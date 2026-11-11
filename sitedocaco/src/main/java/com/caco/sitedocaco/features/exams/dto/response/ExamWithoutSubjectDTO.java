package com.caco.sitedocaco.features.exams.dto.response;

import com.caco.sitedocaco.features.exams.entity.ExamType;

import java.util.UUID;

public record ExamWithoutSubjectDTO(
        UUID id,
        Integer year,
        ExamType type,
        String fileUrl
) {
}
