package com.caco.sitedocaco.modules.manual.dto.response;

import com.caco.sitedocaco.modules.manual.entity.ArticleFeedback;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArticleFeedbackDTO(
        UUID id,
        Boolean isHelpful,
        String comment,
        LocalDateTime postedAt,

        // Informações do usuário
        UUID userId,
        String userName,
        String userEmail,
        String userAvatarUrl,

        // Informações do artigo (opcional, para contexto)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        UUID articleId,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String articleTitle
) {
    public static ArticleFeedbackDTO fromEntity(ArticleFeedback feedback) {
        return new ArticleFeedbackDTO(
                feedback.getId(),
                feedback.getIsHelpful(),
                feedback.getComment(),
                feedback.getPostedAt(),
                feedback.getUser() != null ? feedback.getUser().getId() : null,
                feedback.getUser() != null ? feedback.getUser().getUsername() : "Anônimo",
                feedback.getUser() != null ? feedback.getUser().getEmail() : null,
                feedback.getUser() != null ? feedback.getUser().getAvatarUrl() : null,
                feedback.getArticle().getId(),
                feedback.getArticle().getTitle()
        );
    }

    // Método alternativo sem informações do artigo
    public static ArticleFeedbackDTO simpleFromEntity(ArticleFeedback feedback) {
        return new ArticleFeedbackDTO(
                feedback.getId(),
                feedback.getIsHelpful(),
                feedback.getComment(),
                feedback.getPostedAt(),
                feedback.getUser() != null ? feedback.getUser().getId() : null,
                feedback.getUser() != null ? feedback.getUser().getUsername() : "Anônimo",
                feedback.getUser() != null ? feedback.getUser().getEmail() : null,
                feedback.getUser() != null ? feedback.getUser().getAvatarUrl() : null,
                null,
                null
        );
    }
}