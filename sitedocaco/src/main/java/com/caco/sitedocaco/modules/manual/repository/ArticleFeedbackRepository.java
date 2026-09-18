package com.caco.sitedocaco.modules.manual.repository;

import com.caco.sitedocaco.modules.manual.entity.ArticleFeedback;
import com.caco.sitedocaco.modules.manual.entity.ManualArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleFeedbackRepository extends JpaRepository<ArticleFeedback, UUID> {
    List<ArticleFeedback> findByArticle(ManualArticle article);
    Page<ArticleFeedback> findByArticleOrderByPostedAtDesc(ManualArticle article, Pageable pageable);

    // Contagem de feedbacks úteis/não úteis
    @Query("SELECT COUNT(f) FROM ArticleFeedback f WHERE f.article = ?1 AND f.isHelpful = true")
    long countHelpfulByArticle(ManualArticle article);

    @Query("SELECT COUNT(f) FROM ArticleFeedback f WHERE f.article = ?1 AND f.isHelpful = false")
    long countUnhelpfulByArticle(ManualArticle article);

    // Método alternativo para contagem direta
    long countByArticleAndIsHelpful(ManualArticle article, boolean isHelpful);
}