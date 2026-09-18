package com.caco.sitedocaco.modules.manual.repository;

import com.caco.sitedocaco.modules.manual.entity.ManualCategory;
import com.caco.sitedocaco.modules.manual.entity.ManualChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManualChapterRepository extends JpaRepository<ManualChapter, UUID> {
    Optional<ManualChapter> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<ManualChapter> findByCategoryOrderByOrderAsc(ManualCategory category);
    List<ManualChapter> findByCategoryIdOrderByOrderAsc(UUID categoryId);

    @Query("SELECT MAX(c.order) FROM ManualChapter c WHERE c.category = ?1")
    Integer findMaxOrderByCategory(ManualCategory category);

    // Métodos adicionais necessários
    long countByCategory(ManualCategory category);
    List<ManualChapter> findByCategory(ManualCategory category);
}