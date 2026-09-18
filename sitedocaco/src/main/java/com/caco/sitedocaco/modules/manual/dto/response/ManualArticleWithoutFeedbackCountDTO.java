package com.caco.sitedocaco.modules.manual.dto.response;

import com.caco.sitedocaco.modules.manual.entity.ManualArticle;

import java.util.UUID;

public record ManualArticleWithoutFeedbackCountDTO (
        UUID id,
        String title,
        String slug,
        String content,
        Integer order,
        UUID chapterId,
        String chapterTitle,
        UUID categoryId,
        String categoryTitle
) {
    public static ManualArticleWithoutFeedbackCountDTO fromEntity(ManualArticle article) {
        return new ManualArticleWithoutFeedbackCountDTO(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getContent(),
                article.getOrder(),
                article.getChapter().getId(),
                article.getChapter().getTitle(),
                article.getChapter().getCategory().getId(),
                article.getChapter().getCategory().getTitle()
        );
    }

    public static ManualArticleWithoutFeedbackCountDTO fromManualArticleDTO(ManualArticleDTO articleDTO) {
        return new ManualArticleWithoutFeedbackCountDTO(
                articleDTO.id(),
                articleDTO.title(),
                articleDTO.slug(),
                articleDTO.content(),
                articleDTO.order(),
                articleDTO.chapterId(),
                articleDTO.chapterTitle(),
                articleDTO.categoryId(),
                articleDTO.categoryTitle()
        );
    }
}