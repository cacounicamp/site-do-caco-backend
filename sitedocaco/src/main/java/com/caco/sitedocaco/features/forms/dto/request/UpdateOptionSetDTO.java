package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Substituição completa do conjunto (PUT). As opções são casadas pelo código: novas são criadas,
 * existentes atualizadas e a ordem da lista vira a ordem de exibição. Opções ausentes da lista são
 * excluídas, ou apenas desativadas se já tiverem sido usadas em respostas.
 */
public record UpdateOptionSetDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String name,

        @NotEmpty(message = "Informe ao menos uma opção")
        List<@Valid OptionInputDTO> options
) {
}
