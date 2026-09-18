package com.caco.sitedocaco.modules.manual.controller;

import com.caco.sitedocaco.modules.manual.dto.request.*;
import com.caco.sitedocaco.modules.manual.dto.response.*;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.manual.service.ArticleFeedbackService;
import com.caco.sitedocaco.modules.manual.service.ManualArticleService;
import com.caco.sitedocaco.modules.manual.service.ManualCategoryService;
import com.caco.sitedocaco.modules.manual.service.ManualChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/manual")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 30, refillTokens = 30, refillPeriod = 1)
public class ManualAdminController {

    private final ManualCategoryService categoryService;
    private final ManualChapterService chapterService;
    private final ManualArticleService articleService;
    private final ArticleFeedbackService feedbackService;

    // ==================== CATEGORIAS ====================

    @PostMapping("/categories")
    public ResponseEntity<ManualCategoryDTO> createCategory(@RequestBody @Valid CreateManualCategoryDTO dto) {
        var created = categoryService.createCategory(dto);
        var dtoResponse = ManualCategoryDTO.fromEntity(created, 0L); // 0 chapters inicialmente
        return ResponseEntity
                .created(URI.create("/public/manual/categories/" + created.getSlug()))
                .body(dtoResponse);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ManualCategoryDTO> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateManualCategoryDTO dto) {
        var updated = categoryService.updateCategory(id, dto);
        Long chapterCount = chapterService.countChaptersByCategory(id); // Contar capítulos
        var dtoResponse = ManualCategoryDTO.fromEntity(updated, chapterCount);
        return ResponseEntity.ok(dtoResponse);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/categories/r/reorder")
    public ResponseEntity<Void> reorderCategories(@RequestBody @Valid ReorderCategoriesDTO dto) {
        categoryService.reorderCategories(dto.categoryIds());
        return ResponseEntity.noContent().build();
    }

    // ==================== CAPÍTULOS ====================

    @PostMapping("/chapters")
    public ResponseEntity<ManualChapterDTO> createChapter(@RequestBody @Valid CreateManualChapterDTO dto) {
        var created = chapterService.createChapter(dto);
        Long articleCount = articleService.countArticlesByChapter(created.getId()); // 0 articles inicialmente
        var dtoResponse = ManualChapterDTO.fromEntity(created, articleCount);
        return ResponseEntity
                .created(URI.create("/public/manual/chapters/" + created.getSlug()))
                .body(dtoResponse);
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<ManualChapterDTO> updateChapter(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateManualChapterDTO dto) {
        var updated = chapterService.updateChapter(id, dto);
        Long articleCount = articleService.countArticlesByChapter(id);
        var dtoResponse = ManualChapterDTO.fromEntity(updated, articleCount);
        return ResponseEntity.ok(dtoResponse);
    }

    @DeleteMapping("/chapters/{id}")
    public ResponseEntity<Void> deleteChapter(@PathVariable UUID id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/chapters/r/reorder")
    public ResponseEntity<Void> reorderChapters(@RequestBody @Valid ReorderChaptersDTO dto) {
        chapterService.reorderChapters(dto.categoryId(), dto.chapterIds());
        return ResponseEntity.noContent().build();
    }

    // ==================== ARTIGOS ====================

    @PostMapping("/articles")
    public ResponseEntity<ManualArticleDTO> createArticle(@RequestBody @Valid CreateManualArticleDTO dto) {
        var created = articleService.createArticle(dto);
        var dtoResponse = ManualArticleDTO.fromEntity(created, 0L, 0L); // 0 feedbacks inicialmente
        return ResponseEntity
                .created(URI.create("/public/manual/articles/" + created.getSlug()))
                .body(dtoResponse);
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<ManualArticleDTO> updateArticle(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateManualArticleDTO dto) {
        var updated = articleService.updateArticle(id, dto);
        var dtoResponse = articleService.getArticleById(id); // Já inclui contagem de feedbacks
        return ResponseEntity.ok(dtoResponse);
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/articles/r/reorder")
    public ResponseEntity<Void> reorderArticles(@RequestBody @Valid ReorderArticlesDTO dto) {
        articleService.reorderArticles(dto.chapterId(), dto.articleIds());
        return ResponseEntity.noContent().build();
    }

    // ==================== FEEDBACK (apenas visualização e deleção para admin) ====================

    @GetMapping("/articles/{articleId}/feedback")
    public ResponseEntity<Page<ArticleFeedbackDTO>> getArticleFeedback(
            @PathVariable UUID articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ArticleFeedbackDTO> feedback = feedbackService.getFeedbackByArticle(articleId, page, size);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/articles/{articleId}/feedback/stats")
    public ResponseEntity<Map<String, Object>> getArticleFeedbackStats(@PathVariable UUID articleId) {
        Map<String, Object> stats = feedbackService.getFeedbackStats(articleId);
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
