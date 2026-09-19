package com.caco.sitedocaco.features.forms.dto.request;

import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Substituição completa da configuração da pergunta (PUT). O código nunca muda; tipo e conjunto de
 * opções só mudam enquanto a pergunta não tiver respostas.
 */
public record UpdateFormQuestionDTO(
        @NotBlank(message = "O enunciado é obrigatório")
        @Size(max = 300, message = "O enunciado deve ter no máximo 300 caracteres")
        String prompt,

        @Size(max = 500, message = "O texto de ajuda deve ter no máximo 500 caracteres")
        String helpText,

        @NotNull(message = "O tipo é obrigatório")
        QuestionType type,

        /** Padrão: false. */
        Boolean required,

        /** Se omitido, mantém o valor atual. */
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
