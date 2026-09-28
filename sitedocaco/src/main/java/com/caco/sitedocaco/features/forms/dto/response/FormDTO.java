package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormStatus;

import java.util.List;

/** Definição pública do formulário: só perguntas e opções ativas. */
public record FormDTO(
        String slug,
        String name,
        String description,
        FormStatus status,
        boolean allowEditAfterSubmit,
        /** Indica se o front deve usar /user/forms/{slug}/submission (false) ou /submissions (true). */
        boolean allowMultipleSubmissions,
        Integer maxSubmissionsPerUser,
        List<QuestionDTO> questions
) {
    public static FormDTO from(Form form, List<QuestionDTO> questions) {
        return new FormDTO(
                form.getSlug(),
                form.getName(),
                form.getDescription(),
                form.getStatus(),
                form.isAllowEditAfterSubmit(),
                form.isAllowMultipleSubmissions(),
                form.getMaxSubmissionsPerUser(),
                questions
        );
    }
}
