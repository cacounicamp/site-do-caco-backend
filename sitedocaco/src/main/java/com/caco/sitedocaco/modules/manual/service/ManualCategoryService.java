package com.caco.sitedocaco.modules.manual.service;

import com.caco.sitedocaco.modules.manual.dto.request.CreateManualCategoryDTO;
import com.caco.sitedocaco.modules.manual.dto.request.UpdateManualCategoryDTO;
import com.caco.sitedocaco.modules.manual.dto.response.ManualCategoryDTO;
import com.caco.sitedocaco.modules.manual.entity.ManualCategory;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.manual.repository.ManualCategoryRepository;
import com.caco.sitedocaco.modules.manual.repository.ManualChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualCategoryService {

    private final ManualCategoryRepository categoryRepository;
    private final ManualChapterRepository chapterRepository;

    @Transactional
    public ManualCategory createCategory(CreateManualCategoryDTO dto) {
        // Verificar se slug já existe
        if (categoryRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe uma categoria com este slug");
        }

        ManualCategory category = new ManualCategory();
        category.setTitle(dto.title());
        category.setSlug(dto.slug());

        // Ordenação automática
        Integer maxOrder = categoryRepository.findMaxOrder();
        category.setOrder(maxOrder == null ? 0 : maxOrder + 1);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<ManualCategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByOrderAsc().stream()
                .map(category -> {
                    Long chapterCount = chapterRepository.countByCategory(category);
                    return ManualCategoryDTO.fromEntity(category, chapterCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ManualCategory getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    @Transactional(readOnly = true)
    public ManualCategory getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    @Transactional
    public ManualCategory updateCategory(UUID id, UpdateManualCategoryDTO dto) {
        ManualCategory category = getCategoryById(id);

        if (dto.title() != null) {
            category.setTitle(dto.title());
        }

        if (dto.slug() != null && !dto.slug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(dto.slug())) {
                throw new BusinessRuleException("Já existe uma categoria com este slug");
            }
            category.setSlug(dto.slug());
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        ManualCategory category = getCategoryById(id);

        // Verificar se tem capítulos antes de deletar
        if (!chapterRepository.findByCategory(category).isEmpty()) {
            throw new BusinessRuleException("Não é possível deletar uma categoria que possui capítulos");
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public void reorderCategories(List<UUID> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            UUID id = categoryIds.get(i);
            int finalI = i;
            categoryRepository.findById(id).ifPresent(category -> {
                category.setOrder(finalI);
                categoryRepository.save(category);
            });
        }
    }
}