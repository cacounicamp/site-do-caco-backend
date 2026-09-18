package com.caco.sitedocaco.modules.exams.dto.request;

import com.caco.sitedocaco.modules.exams.entity.ExamType;

import java.util.UUID;

public record UpdateExamDTO(
        String subjectCode,
        UUID professorId,
        Boolean removeProfessor,
        Integer year,
        ExamType type,
        String fileUrl
) {}