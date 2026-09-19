package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;

import java.util.List;

public record QuestionDTO(
        String code,
        String prompt,
        String helpText,
        QuestionType type,
        boolean required,
        String section,
        TextFormat textFormat,
        Integer minValue,
        Integer maxValue,
        List<OptionDTO> options,
        ConditionDTO showIf
) {
    public static QuestionDTO from(FormQuestion question, List<FormOption> activeOptions) {
        return new QuestionDTO(
                question.getCode(),
                question.getPrompt(),
                question.getHelpText(),
                question.getType(),
                question.isRequired(),
                question.getSection(),
                question.getTextFormat(),
                question.getMinValue(),
                question.getMaxValue(),
                activeOptions.stream().map(OptionDTO::fromEntity).toList(),
                ConditionDTO.fromQuestion(question)
        );
    }
}
