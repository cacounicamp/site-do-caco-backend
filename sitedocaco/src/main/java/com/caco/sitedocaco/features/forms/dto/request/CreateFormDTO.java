package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFormDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "O slug é obrigatório")
        @Size(max = 80, message = "O slug deve ter no máximo 80 caracteres")
        @Pattern(regexp = FormValidation.SLUG_REGEX, message = FormValidation.SLUG_MESSAGE)
        String slug,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        /** Padrão: true. */
        Boolean allowEditAfterSubmit
) {
}
