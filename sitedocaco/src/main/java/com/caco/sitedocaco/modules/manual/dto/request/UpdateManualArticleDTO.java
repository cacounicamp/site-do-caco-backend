package com.caco.sitedocaco.modules.manual.dto.request;

import java.util.UUID;

public record UpdateManualArticleDTO(
        String title,
        String slug,
        String content,
        UUID chapterId
) {}