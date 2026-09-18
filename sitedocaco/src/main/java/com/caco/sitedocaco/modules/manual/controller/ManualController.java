package com.caco.sitedocaco.modules.manual.controller;

import com.caco.sitedocaco.modules.manual.dto.response.*;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.manual.service.ArticleFeedbackService;
import com.caco.sitedocaco.modules.manual.service.ManualArticleService;
import com.caco.sitedocaco.modules.manual.service.ManualCategoryService;
import com.caco.sitedocaco.modules.manual.service.ManualChapterService;
import com.caco.sitedocaco.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public/manual")
@RequiredArgsConstructor
@RateLimit
public class ManualController {

    private final ManualCategoryService categoryService;
    private final ManualChapterService chapterService;
    private final ManualArticleService articleService;
    private final ArticleFeedbackService feedbackService;
    private final UserService userService;

    // ==================== CATEGORIAS ====================

    @GetMapping("/categories")
    public ResponseEntity<List<ManualCategoryDTO>> getAllCategories() {
        List<ManualCategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{slug}")
    public ResponseEntity<ManualCategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ManualCategoryDTO.fromEntity(
                categoryService.getCategoryBySlug(slug),
                null
        ));
    }

    // ==================== CAPÍTULOS ====================

    @GetMapping("/chapters")
    public ResponseEntity<List<ManualChapterDTO>> getAllChapters() {
        List<ManualChapterDTO> chapters = chapterService.getAllChapters();
        return ResponseEntity.ok(chapters);
    }

    @GetMapping("/chapters/category/{categoryId}")
    public ResponseEntity<List<ManualChapterDTO>> getChaptersByCategory(@PathVariable UUID categoryId) {
        List<ManualChapterDTO> chapters = chapterService.getChaptersByCategory(categoryId);
        return ResponseEntity.ok(chapters);
    }

    @GetMapping("/chapters/{slug}")
    public ResponseEntity<ManualChapterDTO> getChapterBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ManualChapterDTO.fromEntity(
                chapterService.getChapterBySlug(slug),
                null
        ));
    }

    // ==================== ARTIGOS ====================

    @GetMapping("/articles")
    public ResponseEntity<List<ManualArticleWithoutFeedbackCountDTO>> getAllArticles() {
        List<ManualArticleWithoutFeedbackCountDTO> articles = articleService.getAllArticles().stream()
                .map(ManualArticleWithoutFeedbackCountDTO::fromManualArticleDTO)
                .toList();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles/chapter/{chapterId}")
    public ResponseEntity<List<ManualArticleWithoutFeedbackCountDTO>> getArticlesByChapter(@PathVariable UUID chapterId) {
        List<ManualArticleWithoutFeedbackCountDTO> articles = articleService.getArticlesByChapter(chapterId).stream()
                .map(ManualArticleWithoutFeedbackCountDTO::fromManualArticleDTO)
                .toList();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ManualArticleWithoutFeedbackCountDTO> getArticleById(@PathVariable UUID id) {
        ManualArticleWithoutFeedbackCountDTO article = ManualArticleWithoutFeedbackCountDTO.fromManualArticleDTO( articleService.getArticleById(id));
        return ResponseEntity.ok(article);
    }

    @GetMapping("/articles/slug/{slug}")
    public ResponseEntity<ManualArticleWithoutFeedbackCountDTO> getArticleBySlug(@PathVariable String slug) {
        ManualArticleWithoutFeedbackCountDTO article = ManualArticleWithoutFeedbackCountDTO.fromManualArticleDTO( articleService.getArticleBySlug(slug));
        return ResponseEntity.ok(article);
    }
}
