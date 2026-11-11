package com.caco.sitedocaco.features.exams.dto.request;

import com.caco.sitedocaco.features.exams.entity.ExamType;

import java.util.UUID;

public record UpdateExamDTO(
        String subjectCode,
        UUID professorId,
        Boolean removeProfessor,
        Integer year,
        ExamType type,
        String fileUrl
) {}