package com.caco.sitedocaco.modules.manual.dto.response;

import com.caco.sitedocaco.modules.manual.entity.ManualCategory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ManualCategoryDTO(
        UUID id,
        String title,
        String slug,
        Integer order,

        @JsonProperty("chapterCount")
        Long chapterCount
) {
    public static ManualCategoryDTO fromEntity(ManualCategory category, Long chapterCount) {
        return new ManualCategoryDTO(
                category.getId(),
                category.getTitle(),
                category.getSlug(),
                category.getOrder(),
                chapterCount
        );
    }
}