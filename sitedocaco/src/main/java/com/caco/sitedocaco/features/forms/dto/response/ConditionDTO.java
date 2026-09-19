package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormQuestion;

/** "Mostrar se, na pergunta {@code question}, a opção {@code option} estiver marcada" (option nulo = qualquer). */
public record ConditionDTO(String question, String option) {

    public static ConditionDTO fromQuestion(FormQuestion question) {
        if (question.getShowIfQuestion() == null) {
            return null;
        }
        return new ConditionDTO(
                question.getShowIfQuestion().getCode(),
                question.getShowIfOption() != null ? question.getShowIfOption().getCode() : null
        );
    }
}
