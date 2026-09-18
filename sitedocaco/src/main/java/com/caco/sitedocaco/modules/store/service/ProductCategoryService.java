package com.caco.sitedocaco.modules.store.service;

import com.caco.sitedocaco.modules.store.dto.request.CreateProductCategoryDTO;
import com.caco.sitedocaco.modules.store.dto.request.UpdateProductCategoryDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductCategoryDTO;
import com.caco.sitedocaco.modules.store.entity.ProductCategory;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.store.repository.ProductCategoryRepository;
import com.caco.sitedocaco.modules.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // Admin: todas as categorias
    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByOrderAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    // Público: categorias com produtos ativos
    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getCategoriesWithActiveProducts() {
        return categoryRepository.findCategoriesWithActiveProducts().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductCategoryDTO getCategoryById(UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return toDTO(category);
    }

    @Transactional(readOnly = true)
    public ProductCategory getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    @Transactional(readOnly = true)
    public ProductCategoryDTO getCategoryBySlug(String slug) {
        ProductCategory category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return toDTO(category);
    }

    @Transactional
    public ProductCategoryDTO createCategory(CreateProductCategoryDTO dto) {
        if (categoryRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe uma categoria com este slug");
        }

        ProductCategory category = new ProductCategory();
        category.setName(dto.name());
        category.setSlug(dto.slug());

        // Definir ordem (última posição)
        Integer maxOrder = categoryRepository.findMaxOrder();
        category.setOrder(maxOrder == null ? 0 : maxOrder + 1);

        ProductCategory saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Transactional
    public ProductCategoryDTO updateCategory(UUID id, UpdateProductCategoryDTO dto) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        if (dto.name() != null && !dto.name().isBlank()) {
            category.setName(dto.name());
        }

        if (dto.slug() != null && !dto.slug().isBlank() && !dto.slug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(dto.slug())) {
                throw new BusinessRuleException("Já existe uma categoria com este slug");
            }
            category.setSlug(dto.slug());
        }

        ProductCategory saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        // Verificar se há produtos nesta categoria
        if (productRepository.countByCategoryIdAndActiveTrue(id) > 0) {
            throw new BusinessRuleException("Não é possível excluir categoria com produtos ativos");
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

    private ProductCategoryDTO toDTO(ProductCategory category) {
        return new ProductCategoryDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getOrder()
        );
    }
}