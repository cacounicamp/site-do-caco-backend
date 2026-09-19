package com.caco.sitedocaco.features.forms.dto.request;

import com.caco.sitedocaco.features.forms.entity.FormStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Substituição completa dos dados do formulário (PUT). As perguntas têm endpoints próprios. */
public record UpdateFormDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "O slug é obrigatório")
        @Size(max = 80, message = "O slug deve ter no máximo 80 caracteres")
        @Pattern(regexp = FormValidation.SLUG_REGEX, message = FormValidation.SLUG_MESSAGE)
        String slug,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        @NotNull(message = "O status é obrigatório")
        FormStatus status,

        @NotNull(message = "O campo allowEditAfterSubmit é obrigatório")
        Boolean allowEditAfterSubmit
) {
}
