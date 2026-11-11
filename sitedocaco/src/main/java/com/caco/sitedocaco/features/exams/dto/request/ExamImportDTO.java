package com.caco.sitedocaco.features.exams.dto.request;

public record ExamImportDTO(String subjectCode, String professorId, Integer year, String type, String fileUrl) {}
