package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormAnswer;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Só o campo correspondente ao tipo da pergunta vem preenchido. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnswerDTO(
        String text,
        List<SelectedOptionDTO> options,
        String fileName,
        String fileUrl
) {
    public static AnswerDTO from(FormAnswer answer) {
        return switch (answer.getQuestion().getType()) {
            case SINGLE_CHOICE, MULTIPLE_CHOICE -> new AnswerDTO(
                    null,
                    answer.getOptions().stream().map(SelectedOptionDTO::fromEntity).toList(),
                    null, null);
            case FILE -> new AnswerDTO(null, null, answer.getFileName(), answer.getFileUrl());
            case TEXT, LONG_TEXT -> new AnswerDTO(answer.getTextValue(), null, null, null);
        };
    }
}
