package com.caco.sitedocaco.modules.exams.dto.request;

import java.util.List;

public record ImportPayloadDTO(
        List<CreateSubjectDTO> subjects,
        List<ProfessorImportDTO> professors,
        List<ExamImportDTO> exams
) {}
