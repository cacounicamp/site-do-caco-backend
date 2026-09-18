package com.caco.sitedocaco.modules.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateProductCategoryDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "O slug é obrigatório")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
        String slug
) {}