package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Resposta a uma pergunta de texto (text) ou de escolha (options). Arquivos têm endpoint próprio. */
public record AnswerInputDTO(
        @NotBlank(message = "O código da pergunta é obrigatório")
        String question,

        String text,

        List<@Valid ChoiceInputDTO> options
) {
}
