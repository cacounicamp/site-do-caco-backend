package com.caco.sitedocaco.modules.manual.service;

import com.caco.sitedocaco.modules.manual.dto.request.CreateManualChapterDTO;
import com.caco.sitedocaco.modules.manual.dto.request.UpdateManualChapterDTO;
import com.caco.sitedocaco.modules.manual.dto.response.ManualChapterDTO;
import com.caco.sitedocaco.modules.manual.entity.ManualCategory;
import com.caco.sitedocaco.modules.manual.entity.ManualChapter;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.manual.repository.ManualArticleRepository;
import com.caco.sitedocaco.modules.manual.repository.ManualCategoryRepository;
import com.caco.sitedocaco.modules.manual.repository.ManualChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualChapterService {

    private final ManualChapterRepository chapterRepository;
    private final ManualCategoryRepository categoryRepository;
    private final ManualArticleRepository articleRepository;
    private final ManualCategoryService categoryService;

    @Transactional(readOnly = true)
    public long countChaptersByCategory(UUID categoryId) {
        ManualCategory category = categoryService.getCategoryById(categoryId);
        return chapterRepository.countByCategory(category);
    }

    @Transactional
    public void deleteChapter(UUID id) {
        ManualChapter chapter = getChapterById(id);

        // Verificar se tem artigos antes de deletar
        if (!articleRepository.findByChapter(chapter).isEmpty()) {
            throw new BusinessRuleException("Não é possível deletar um capítulo que possui artigos");
        }

        chapterRepository.delete(chapter);
    }

    @Transactional
    public void reorderChapters(UUID categoryId, List<UUID> chapterIds) {
        for (UUID chapterId : chapterIds) {
            chapterRepository.findById(chapterId).ifPresent(chapter -> {
                if (!chapter.getCategory().getId().equals(categoryId)) {
                    throw new BusinessRuleException(
                            String.format("O capítulo %s não pertence à categoria %s",
                                    chapterId, categoryId)
                    );
                }
            });
        }

        // Reordenar
        for (int i = 0; i < chapterIds.size(); i++) {
            UUID id = chapterIds.get(i);
            int finalI = i;
            chapterRepository.findById(id).ifPresent(chapter -> {
                chapter.setOrder(finalI);
                chapterRepository.save(chapter);
            });
        }
    }

    @Transactional
    public ManualChapter createChapter(CreateManualChapterDTO dto) {
        // Verificar se slug já existe
        if (chapterRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe um capítulo com este slug");
        }

        ManualCategory category = categoryService.getCategoryById(dto.categoryId());

        ManualChapter chapter = new ManualChapter();
        chapter.setTitle(dto.title());
        chapter.setSlug(dto.slug());
        chapter.setCategory(category);

        // Ordenação automática dentro da categoria
        Integer maxOrder = chapterRepository.findMaxOrderByCategory(category);
        chapter.setOrder(maxOrder == null ? 0 : maxOrder + 1);

        return chapterRepository.save(chapter);
    }

    @Transactional(readOnly = true)
    public List<ManualChapterDTO> getAllChapters() {
        return chapterRepository.findAll().stream()
                .map(chapter -> {
                    Long articleCount = articleRepository.countByChapter(chapter);
                    return ManualChapterDTO.fromEntity(chapter, articleCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManualChapterDTO> getChaptersByCategory(UUID categoryId) {
        ManualCategory category = categoryService.getCategoryById(categoryId);
        return chapterRepository.findByCategoryOrderByOrderAsc(category).stream()
                .map(chapter -> {
                    Long articleCount = articleRepository.countByChapter(chapter);
                    return ManualChapterDTO.fromEntity(chapter, articleCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ManualChapter getChapterById(UUID id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capítulo não encontrado"));
    }

    @Transactional(readOnly = true)
    public ManualChapter getChapterBySlug(String slug) {
        return chapterRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Capítulo não encontrado"));
    }

    @Transactional
    public ManualChapter updateChapter(UUID id, UpdateManualChapterDTO dto) {
        ManualChapter chapter = getChapterById(id);

        if (dto.title() != null) {
            chapter.setTitle(dto.title());
        }

        if (dto.slug() != null && !dto.slug().equals(chapter.getSlug())) {
            if (chapterRepository.existsBySlug(dto.slug())) {
                throw new BusinessRuleException("Já existe um capítulo com este slug");
            }
            chapter.setSlug(dto.slug());
        }

        if (dto.categoryId() != null) {
            ManualCategory newCategory = categoryService.getCategoryById(dto.categoryId());
            chapter.setCategory(newCategory);
        }

        return chapterRepository.save(chapter);
    }
}