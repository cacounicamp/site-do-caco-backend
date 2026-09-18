package com.caco.sitedocaco.modules.manual.service;

import com.caco.sitedocaco.modules.manual.dto.request.CreateManualArticleDTO;
import com.caco.sitedocaco.modules.manual.dto.request.UpdateManualArticleDTO;
import com.caco.sitedocaco.modules.manual.dto.response.ManualArticleDTO;
import com.caco.sitedocaco.modules.manual.entity.ArticleFeedback;
import com.caco.sitedocaco.modules.manual.entity.ManualArticle;
import com.caco.sitedocaco.modules.manual.entity.ManualChapter;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.manual.repository.ArticleFeedbackRepository;
import com.caco.sitedocaco.modules.manual.repository.ManualArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualArticleService {

    private final ManualArticleRepository articleRepository;
    private final ArticleFeedbackRepository feedbackRepository;
    private final ManualChapterService chapterService;

    @Transactional
    public ManualArticle createArticle(CreateManualArticleDTO dto) {
        // Verificar se slug já existe
        if (articleRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe um artigo com este slug");
        }

        ManualChapter chapter = chapterService.getChapterById(dto.chapterId());

        ManualArticle article = new ManualArticle();
        article.setTitle(dto.title());
        article.setSlug(dto.slug());
        article.setContent(dto.content());
        article.setChapter(chapter);

        // Ordenação automática dentro do capítulo
        Integer maxOrder = articleRepository.findMaxOrderByChapter(chapter);
        article.setOrder(maxOrder == null ? 0 : maxOrder + 1);

        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public List<ManualArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(article -> {
                    Long helpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, true);
                    Long unhelpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, false);
                    return ManualArticleDTO.fromEntity(article, helpfulCount, unhelpfulCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManualArticleDTO> getArticlesByChapter(UUID chapterId) {
        ManualChapter chapter = chapterService.getChapterById(chapterId);
        return articleRepository.findByChapterOrderByOrderAsc(chapter).stream()
                .map(article -> {
                    Long helpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, true);
                    Long unhelpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, false);
                    return ManualArticleDTO.fromEntity(article, helpfulCount, unhelpfulCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ManualArticleDTO getArticleById(UUID id) {
        ManualArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        Long helpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, true);
        Long unhelpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, false);

        return ManualArticleDTO.fromEntity(article, helpfulCount, unhelpfulCount);
    }

    @Transactional(readOnly = true)
    public ManualArticle getArticleEntityById(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));
    }

    @Transactional(readOnly = true)
    public ManualArticleDTO getArticleBySlug(String slug) {
        ManualArticle article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        Long helpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, true);
        Long unhelpfulCount = feedbackRepository.countByArticleAndIsHelpful(article, false);

        return ManualArticleDTO.fromEntity(article, helpfulCount, unhelpfulCount);
    }

    @Transactional
    public ManualArticle updateArticle(UUID id, UpdateManualArticleDTO dto) {
        ManualArticle article = getArticleEntityById(id);

        if (dto.title() != null) {
            article.setTitle(dto.title());
        }

        if (dto.slug() != null && !dto.slug().equals(article.getSlug())) {
            if (articleRepository.existsBySlug(dto.slug())) {
                throw new BusinessRuleException("Já existe um artigo com este slug");
            }
            article.setSlug(dto.slug());
        }

        if (dto.content() != null) {
            article.setContent(dto.content());
        }

        if (dto.chapterId() != null) {
            ManualChapter newChapter = chapterService.getChapterById(dto.chapterId());
            article.setChapter(newChapter);
        }

        return articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(UUID id) {
        ManualArticle article = getArticleEntityById(id);

        // Deletar feedbacks associados primeiro
        List<ArticleFeedback> feedbacks = feedbackRepository.findByArticle(article);
        feedbackRepository.deleteAll(feedbacks);

        articleRepository.delete(article);
    }

    @Transactional
    public void reorderArticles(UUID chapterId, List<UUID> articleIds) {
        ManualChapter chapter = chapterService.getChapterById(chapterId);

        // Verificar se todos os artigos pertencem ao capítulo
        for (UUID articleId : articleIds) {
            articleRepository.findById(articleId).ifPresent(article -> {
                if (!article.getChapter().getId().equals(chapterId)) {
                    throw new BusinessRuleException(
                            String.format("O artigo %s não pertence ao capítulo %s",
                                    articleId, chapterId)
                    );
                }
            });
        }

        // Reordenar
        for (int i = 0; i < articleIds.size(); i++) {
            UUID id = articleIds.get(i);
            int finalI = i;
            articleRepository.findById(id).ifPresent(article -> {
                article.setOrder(finalI);
                articleRepository.save(article);
            });
        }
    }

    @Transactional(readOnly = true)
    public long countArticlesByChapter(UUID chapterId) {
        ManualChapter chapter = chapterService.getChapterById(chapterId);
        return articleRepository.countByChapter(chapter);
    }
}