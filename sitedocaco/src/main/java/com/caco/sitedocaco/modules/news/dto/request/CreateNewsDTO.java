package com.caco.sitedocaco.modules.news.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public record CreateNewsDTO(
        @NotBlank(message = "O título é obrigatório") String title,
        @NotBlank(message = "O slug é obrigatório")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                 message = "Slug deve conter apenas letras minúsculas, números e hífens (sem espaços)")
        String slug,
        @NotBlank(message = "O resumo é obrigatório") String summary,
        @NotBlank(message = "O conteúdo é obrigatório") String content,
        MultipartFile coverImage // Opcional
) {}