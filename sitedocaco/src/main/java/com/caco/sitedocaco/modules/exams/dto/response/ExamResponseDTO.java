// main/java/com.caco.sitedocaco.modules.exams.dto.response.ExamResponseDTO.java
package com.caco.sitedocaco.modules.exams.dto.response;

import com.caco.sitedocaco.modules.exams.entity.ExamType;
import com.caco.sitedocaco.modules.exams.entity.Exam;
import com.caco.sitedocaco.modules.exams.entity.Subject;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExamResponseDTO(
        UUID id,
        String subjectCode,
        String subjectName,
        Integer year,
        ExamType type,
        String fileUrl
) {
    public static ExamResponseDTO fromEntity(Exam exam) {
        Subject subject = exam.getSubject();
        return new ExamResponseDTO(
                exam.getId(),
                subject != null ? subject.getSubjectCode() : null,
                subject != null ? subject.getName() : null,
                exam.getYear(),
                exam.getType(),
                exam.getFileUrl()
        );
    }
}