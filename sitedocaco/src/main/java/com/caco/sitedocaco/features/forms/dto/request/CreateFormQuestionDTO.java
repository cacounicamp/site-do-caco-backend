package com.caco.sitedocaco.features.forms.dto.request;

import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** A pergunta é adicionada ao final do formulário; use o endpoint de reordenação para movê-la. */
public record CreateFormQuestionDTO(
        @NotBlank(message = "O código é obrigatório")
        @Size(max = 60, message = "O código deve ter no máximo 60 caracteres")
        @Pattern(regexp = FormValidation.CODE_REGEX, message = FormValidation.CODE_MESSAGE)
        String code,

        @NotBlank(message = "O enunciado é obrigatório")
        @Size(max = 300, message = "O enunciado deve ter no máximo 300 caracteres")
        String prompt,

        @Size(max = 500, message = "O texto de ajuda deve ter no máximo 500 caracteres")
        String helpText,

        @NotNull(message = "O tipo é obrigatório")
        QuestionType type,

        /** Padrão: false. */
        Boolean required,

        /** Padrão: true. */
        Boolean active,

        @Size(max = 60, message = "A seção deve ter no máximo 60 caracteres")
        String section,

        String optionSetSlug,
        TextFormat textFormat,
        Integer minValue,
        Integer maxValue,
        String showIfQuestion,
        String showIfOption
) implements QuestionConfig {
}
