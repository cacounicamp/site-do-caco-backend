package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Visão de edição: inclui perguntas inativas e os metadados de bloqueio. */
public record FormAdminDTO(
        UUID id,
        String slug,
        String name,
        String description,
        FormStatus status,
        boolean allowEditAfterSubmit,
        long submissionCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<QuestionAdminDTO> questions
) {
    public static FormAdminDTO from(Form form, long submissionCount, List<QuestionAdminDTO> questions) {
        return new FormAdminDTO(
                form.getId(),
                form.getSlug(),
                form.getName(),
                form.getDescription(),
                form.getStatus(),
                form.isAllowEditAfterSubmit(),
                submissionCount,
                form.getCreatedAt(),
                form.getUpdatedAt(),
                questions
        );
    }
}
