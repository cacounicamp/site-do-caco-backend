package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormAnswerOption;

public record SelectedOptionDTO(String code, String label, String freeText) {

    public static SelectedOptionDTO fromEntity(FormAnswerOption answerOption) {
        return new SelectedOptionDTO(
                answerOption.getOption().getCode(),
                answerOption.getOption().getLabel(),
                answerOption.getFreeText()
        );
    }
}
