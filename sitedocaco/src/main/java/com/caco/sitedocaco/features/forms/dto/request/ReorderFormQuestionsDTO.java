package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Lista com TODAS as perguntas do formulário (ativas e inativas), na ordem desejada. */
public record ReorderFormQuestionsDTO(
        @NotEmpty(message = "A lista de perguntas é obrigatória")
        List<@NotNull(message = "ID de pergunta inválido") UUID> questionIds
) {
}
