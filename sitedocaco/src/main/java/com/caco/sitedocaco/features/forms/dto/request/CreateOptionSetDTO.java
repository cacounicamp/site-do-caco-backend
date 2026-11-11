package com.caco.sitedocaco.features.forms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOptionSetDTO(
        @NotBlank(message = "O slug é obrigatório")
        @Size(max = 60, message = "O slug deve ter no máximo 60 caracteres")
        @Pattern(regexp = FormValidation.CODE_REGEX, message = FormValidation.CODE_MESSAGE)
        String slug,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String name,

        /** A ordem da lista define a ordem de exibição. */
        @NotEmpty(message = "Informe ao menos uma opção")
        List<@Valid OptionInputDTO> options
) {
}
