package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OptionInputDTO(
        @NotBlank(message = "O código da opção é obrigatório")
        @Size(max = 60, message = "O código da opção deve ter no máximo 60 caracteres")
        @Pattern(regexp = FormValidation.CODE_REGEX, message = FormValidation.CODE_MESSAGE)
        String code,

        @NotBlank(message = "O rótulo da opção é obrigatório")
        @Size(max = 150, message = "O rótulo da opção deve ter no máximo 150 caracteres")
        String label,

        /** "Outro (especifique)". Padrão: false. */
        Boolean allowsFreeText,

        /** Padrão: true. */
        Boolean active
) {
}
