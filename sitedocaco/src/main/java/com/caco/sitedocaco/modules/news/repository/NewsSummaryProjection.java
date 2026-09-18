package com.caco.sitedocaco.modules.news.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public record NewsSummaryProjection(
        UUID id,
        String title,
        String slug,
        String summary,
        String coverImage,
        LocalDateTime publishDate
) {}
