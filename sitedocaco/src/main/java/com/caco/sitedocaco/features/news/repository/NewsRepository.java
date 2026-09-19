package com.caco.sitedocaco.features.news.repository;

import com.caco.sitedocaco.features.news.dto.response.NewsDetailDTO;
import com.caco.sitedocaco.features.news.dto.response.NewsSummaryDTO;
import com.caco.sitedocaco.features.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsRepository extends JpaRepository<News, UUID> {

    Optional<News> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Optimized Query: Returns DTOs directly, skipping the heavy 'content' field and author data
    @Query("SELECT new com.caco.sitedocaco.features.news.dto.response.NewsSummaryDTO(" +
            "n.id, n.title, n.slug, n.summary, n.coverImage, n.publishDate) " +
            "FROM News n ORDER BY n.publishDate DESC")
    Page<NewsSummaryDTO> findAllSummaries(Pageable pageable);

    // Full news detail by slug (includes content, without author data)
    @Query("SELECT new com.caco.sitedocaco.features.news.dto.response.NewsDetailDTO(" +
            "n.id, n.title, n.slug, n.summary, n.content, n.coverImage, n.publishDate) " +
            "FROM News n WHERE n.slug = :slug")
    Optional<NewsDetailDTO> findDetailBySlug(@Param("slug") String slug);

    // Get all news summaries by author, paginated
    @Query("SELECT new com.caco.sitedocaco.features.news.dto.response.NewsSummaryDTO(" +
            "n.id, n.title, n.slug, n.summary, n.coverImage, n.publishDate) " +
            "FROM News n WHERE n.author.id = :authorId ORDER BY n.publishDate DESC")
    Page<NewsSummaryDTO> findAllByAuthor(@Param("authorId") UUID authorId, Pageable pageable);

    // Get full news detail by slug and author (for permission check)
    @Query("SELECT new com.caco.sitedocaco.features.news.dto.response.NewsDetailDTO(" +
            "n.id, n.title, n.slug, n.summary, n.content, n.coverImage, n.publishDate) " +
            "FROM News n WHERE n.slug = :slug AND n.author.id = :authorId")
    Optional<NewsDetailDTO> findBySlugAndAuthor(@Param("slug") String slug, @Param("authorId") UUID authorId);
}
