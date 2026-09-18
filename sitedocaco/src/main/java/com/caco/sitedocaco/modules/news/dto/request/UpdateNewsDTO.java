package com.caco.sitedocaco.modules.news.dto.request;

import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public record UpdateNewsDTO(
        String title,
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                 message = "Slug deve conter apenas letras minúsculas, números e hífens (sem espaços)")
        String slug,
        String summary,
        String content,
        MultipartFile coverImage,       // nova imagem (opcional)
        Boolean removeCoverImage        // true = remove a imagem atual sem substituir
) {}
