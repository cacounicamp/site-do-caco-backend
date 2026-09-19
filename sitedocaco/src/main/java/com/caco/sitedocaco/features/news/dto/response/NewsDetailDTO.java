package com.caco.sitedocaco.features.news.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NewsDetailDTO(
        UUID id,
        String title,
        String slug,
        String summary,
        String content,
        String coverImage,
        LocalDateTime publishDate
) {}

