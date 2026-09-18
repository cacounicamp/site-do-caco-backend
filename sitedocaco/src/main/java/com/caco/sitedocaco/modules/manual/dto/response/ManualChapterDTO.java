package com.caco.sitedocaco.modules.manual.dto.response;

import com.caco.sitedocaco.modules.manual.entity.ManualChapter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ManualChapterDTO(
        UUID id,
        String title,
        String slug,
        Integer order,
        UUID categoryId,
        String categoryTitle,

        @JsonProperty("articleCount")
        Long articleCount
) {
    public static ManualChapterDTO fromEntity(ManualChapter chapter, Long articleCount) {
        return new ManualChapterDTO(
                chapter.getId(),
                chapter.getTitle(),
                chapter.getSlug(),
                chapter.getOrder(),
                chapter.getCategory().getId(),
                chapter.getCategory().getTitle(),
                articleCount
        );
    }
}