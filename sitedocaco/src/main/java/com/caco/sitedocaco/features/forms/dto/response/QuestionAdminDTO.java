package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;

import java.util.UUID;

public record QuestionAdminDTO(
        UUID id,
        String code,
        String prompt,
        String helpText,
        QuestionType type,
        boolean required,
        boolean active,
        int displayOrder,
        String section,
        String optionSetSlug,
        TextFormat textFormat,
        Integer minValue,
        Integer maxValue,
        ConditionDTO showIf,
        /** Com respostas, o tipo e o conjunto de opções ficam travados e a pergunta não pode ser excluída. */
        boolean hasAnswers
) {
    public static QuestionAdminDTO from(FormQuestion question, boolean hasAnswers) {
        return new QuestionAdminDTO(
                question.getId(),
                question.getCode(),
                question.getPrompt(),
                question.getHelpText(),
                question.getType(),
                question.isRequired(),
                question.isActive(),
                question.getDisplayOrder(),
                question.getSection(),
                question.getOptionSet() != null ? question.getOptionSet().getSlug() : null,
                question.getTextFormat(),
                question.getMinValue(),
                question.getMaxValue(),
                ConditionDTO.fromQuestion(question),
                hasAnswers
        );
    }
}
