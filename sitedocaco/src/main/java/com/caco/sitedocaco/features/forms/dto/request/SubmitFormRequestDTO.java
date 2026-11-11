package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Estado completo das respostas de texto e de escolha: pergunta visível que não vier aqui é
 * considerada sem resposta. Respostas de arquivo não entram (endpoint próprio) e são preservadas.
 */
public record SubmitFormRequestDTO(
        @NotNull(message = "A lista de respostas é obrigatória")
        List<@Valid @NotNull(message = "Resposta inválida") AnswerInputDTO> answers
) {
}
