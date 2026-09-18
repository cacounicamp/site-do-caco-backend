package com.caco.sitedocaco.modules.manual.controller;

import com.caco.sitedocaco.modules.manual.dto.request.CreateArticleFeedbackDTO;
import com.caco.sitedocaco.modules.manual.dto.response.ArticleFeedbackDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.manual.service.ArticleFeedbackService;
import com.caco.sitedocaco.modules.manual.service.ManualArticleService;
import com.caco.sitedocaco.shared.contract.UserAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/article-feedback")
@RequiredArgsConstructor
public class ArticleFeedbackController {
    private final ManualArticleService articleService;
    private final ArticleFeedbackService feedbackService;
    private final UserAccess userAccess;

    // ==================== FEEDBACK (apenas criação) ====================

    // Feedback: 10 envios por minuto por usuário é mais que suficiente
    @RateLimit(capacity = 10, refillTokens = 10)
    @PostMapping("/articles/{articleId}/feedback")
    public ResponseEntity<ArticleFeedbackDTO> createFeedback(
            @PathVariable UUID articleId,
            @RequestBody @Valid CreateArticleFeedbackDTO dto) {

        var feedback = feedbackService.createFeedback(articleId, dto, userAccess.getCurrentUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ArticleFeedbackDTO.fromEntity(feedback));
    }
}
