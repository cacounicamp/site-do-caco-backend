package com.caco.sitedocaco.modules.manual.dto.request;

import java.util.UUID;

public record UpdateManualChapterDTO(
        String title,
        String slug,
        UUID categoryId
) {}