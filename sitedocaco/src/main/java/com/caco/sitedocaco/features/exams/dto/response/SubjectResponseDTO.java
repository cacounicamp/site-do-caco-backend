package com.caco.sitedocaco.features.exams.dto.response;

import com.caco.sitedocaco.features.exams.entity.Subject;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SubjectResponseDTO(
        String subjectCode,
        String name,

        @JsonProperty("examCount")
        Long examCount // Número de provas disponíveis
) {
    public static SubjectResponseDTO fromEntity(Subject subject, Long examCount) {
        return new SubjectResponseDTO(
                subject.getSubjectCode(),
                subject.getName(),
                examCount
        );
    }
}