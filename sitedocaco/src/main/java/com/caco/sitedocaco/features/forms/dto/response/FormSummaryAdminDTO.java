package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record FormSummaryAdminDTO(
        UUID id,
        String slug,
        String name,
        FormStatus status,
        boolean allowEditAfterSubmit,
        long submissionCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FormSummaryAdminDTO from(Form form, long submissionCount) {
        return new FormSummaryAdminDTO(
                form.getId(),
                form.getSlug(),
                form.getName(),
                form.getStatus(),
                form.isAllowEditAfterSubmit(),
                submissionCount,
                form.getCreatedAt(),
                form.getUpdatedAt()
        );
    }
}
