package com.caco.sitedocaco.modules.manual.service;

import com.caco.sitedocaco.modules.manual.dto.request.CreateArticleFeedbackDTO;
import com.caco.sitedocaco.modules.manual.dto.response.ArticleFeedbackDTO;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.shared.contract.UserAccess;
import com.caco.sitedocaco.modules.manual.entity.ArticleFeedback;
import com.caco.sitedocaco.modules.manual.entity.ManualArticle;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.manual.repository.ArticleFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleFeedbackService {

    private final ArticleFeedbackRepository feedbackRepository;
    private final ManualArticleService articleService;
    private final UserAccess userAccess;

    @Transactional
    public ArticleFeedback createFeedback(UUID articleId, CreateArticleFeedbackDTO dto, User user) {
        ManualArticle article = articleService.getArticleEntityById(articleId);

        ArticleFeedback feedback = new ArticleFeedback();
        feedback.setArticle(article);
        feedback.setUser(user);
        feedback.setIsHelpful(dto.isHelpful());
        feedback.setComment(dto.comment());
        feedback.setPostedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    @Transactional
    public ArticleFeedback createFeedback(UUID articleId, CreateArticleFeedbackDTO dto) {
        // Para feedback anônimo
        ManualArticle article = articleService.getArticleEntityById(articleId);

        ArticleFeedback feedback = new ArticleFeedback();
        feedback.setArticle(article);
        feedback.setUser(null); // Anônimo
        feedback.setIsHelpful(dto.isHelpful());
        feedback.setComment(dto.comment());
        feedback.setPostedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public Page<ArticleFeedbackDTO> getFeedbackByArticle(UUID articleId, int page, int size) {
        ManualArticle article = articleService.getArticleEntityById(articleId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt"));

        return feedbackRepository.findByArticleOrderByPostedAtDesc(article, pageable)
                .map(ArticleFeedbackDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ArticleFeedbackDTO> getFeedbackByArticle(UUID articleId, Pageable pageable) {
        ManualArticle article = articleService.getArticleEntityById(articleId);
        return feedbackRepository.findByArticleOrderByPostedAtDesc(article, pageable)
                .map(ArticleFeedbackDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ArticleFeedback getFeedbackById(UUID id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback não encontrado"));
    }

    @Transactional
    public void deleteFeedback(UUID id) {
        ArticleFeedback feedback = getFeedbackById(id);
        feedbackRepository.delete(feedback);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFeedbackStats(UUID articleId) {
        ManualArticle article = articleService.getArticleEntityById(articleId);

        long helpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, true);
        long unhelpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, false);
        long totalCount = helpfulCount + unhelpfulCount;

        double helpfulPercentage = totalCount > 0 ?
                Math.round((helpfulCount * 100.0 / totalCount) * 100.0) / 100.0 : 0.0;

        return Map.of(
                "total", totalCount,
                "helpful", helpfulCount,
                "unhelpful", unhelpfulCount,
                "helpfulPercentage", helpfulPercentage,
                "articleId", articleId,
                "articleTitle", article.getTitle()
        );
    }
}
