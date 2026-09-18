package com.caco.sitedocaco.modules.manual.repository;

import com.caco.sitedocaco.modules.manual.entity.ManualArticle;
import com.caco.sitedocaco.modules.manual.entity.ManualChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManualArticleRepository extends JpaRepository<ManualArticle, UUID> {
    Optional<ManualArticle> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<ManualArticle> findByChapterOrderByOrderAsc(ManualChapter chapter);
    List<ManualArticle> findByChapterIdOrderByOrderAsc(UUID chapterId);

    @Query("SELECT MAX(a.order) FROM ManualArticle a WHERE a.chapter = ?1")
    Integer findMaxOrderByChapter(ManualChapter chapter);

    // Métodos adicionais necessários
    long countByChapter(ManualChapter chapter);
    List<ManualArticle> findByChapter(ManualChapter chapter);
}