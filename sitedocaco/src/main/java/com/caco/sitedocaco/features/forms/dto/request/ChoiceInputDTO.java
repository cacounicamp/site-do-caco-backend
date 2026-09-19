package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChoiceInputDTO(
        @NotBlank(message = "O código da opção é obrigatório")
        String option,

        /** Obrigatório quando a opção é do tipo "Outro (especifique)". */
        String freeText
) {
}
