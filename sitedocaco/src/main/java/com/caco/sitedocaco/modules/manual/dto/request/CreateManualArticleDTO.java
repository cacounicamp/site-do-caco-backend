package com.caco.sitedocaco.modules.manual.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateManualArticleDTO(
        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotBlank(message = "O slug é obrigatório")
        String slug,

        @NotBlank(message = "O conteúdo é obrigatório")
        String content,

        @NotNull(message = "O capítulo é obrigatório")
        UUID chapterId
) {}