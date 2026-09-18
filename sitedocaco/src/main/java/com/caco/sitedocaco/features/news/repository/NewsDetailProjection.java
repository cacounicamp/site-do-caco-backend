package com.caco.sitedocaco.features.news.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public record NewsDetailProjection(
        UUID id,
        String title,
        String slug,
        String summary,
        String content,
        String coverImage,
        LocalDateTime publishDate
) {}
