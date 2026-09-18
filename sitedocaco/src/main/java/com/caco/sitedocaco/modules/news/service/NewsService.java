package com.caco.sitedocaco.modules.news.service;

import com.caco.sitedocaco.modules.news.dto.request.CreateNewsDTO;
import com.caco.sitedocaco.modules.news.dto.request.UpdateNewsDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsDetailDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsSummaryDTO;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.modules.news.entity.News;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import com.caco.sitedocaco.modules.news.repository.NewsRepository;
import com.caco.sitedocaco.modules.news.repository.NewsDetailProjection;
import com.caco.sitedocaco.modules.news.repository.NewsSummaryProjection;
import com.caco.sitedocaco.shared.contract.NewsSummaryProvider;
import com.caco.sitedocaco.shared.contract.UserAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsService implements NewsSummaryProvider {

    private final NewsRepository newsRepository;
    private final UserAccess userAccess;
    private final ImgBBService imgBBService;

    @Transactional(readOnly = true)
    public List<NewsSummaryDTO> getLatestNews(int limit) {
        return newsRepository.findAllSummaries(PageRequest.of(0, limit)).map(this::toSummary).getContent();
    }

    @Transactional(readOnly = true)
    public Page<NewsSummaryDTO> getAllNews(Pageable pageable) {
        return newsRepository.findAllSummaries(pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public NewsDetailDTO getNewsBySlug(String slug) {
        return newsRepository.findDetailBySlug(slug).map(this::toDetail)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<NewsSummaryDTO> getNewsByAuthor(UUID authorId, Pageable pageable) {
        return newsRepository.findAllByAuthor(authorId, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public NewsDetailDTO getNewsBySlugAndAuthor(String slug, UUID authorId) {
        return newsRepository.findBySlugAndAuthor(slug, authorId).map(this::toDetail)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada ou você não é o autor"));
    }

    @Transactional
    public NewsDetailDTO createNews(CreateNewsDTO dto, UUID authorId) throws IOException {
        User author = userAccess.getUserById(authorId);

        // Valida unicidade do slug
        if (newsRepository.existsBySlug(dto.slug())) {
            throw new IllegalArgumentException("Já existe uma notícia com este slug: " + dto.slug());
        }

        News news = new News();
        news.setTitle(dto.title());
        news.setSlug(dto.slug());
        news.setSummary(dto.summary());
        news.setContent(dto.content());
        news.setAuthor(author);
        news.setPublishDate(LocalDateTime.now());

        if (dto.coverImage() != null && !dto.coverImage().isEmpty()) {
            String url = imgBBService.uploadImage(dto.coverImage(), ImageType.NEWS_COVER);
            news.setCoverImage(url);
        }

        return toDetailDTO(newsRepository.save(news));
    }

    @Transactional
    public NewsDetailDTO updateNews(UUID id, UpdateNewsDTO dto, UUID requesterId, boolean isAdmin) throws IOException {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada"));

        validateOwnership(news, requesterId, isAdmin);

        if (dto.title() != null && !dto.title().isBlank()) {
            news.setTitle(dto.title());
        }

        if (dto.slug() != null && !dto.slug().isBlank()) {
            // Se o slug está sendo alterado, valida unicidade
            if (!dto.slug().equals(news.getSlug()) && newsRepository.existsBySlug(dto.slug())) {
                throw new IllegalArgumentException("Já existe uma notícia com este slug: " + dto.slug());
            }
            news.setSlug(dto.slug());
        }

        if (dto.summary() != null) news.setSummary(dto.summary());
        if (dto.content() != null) news.setContent(dto.content());

        // Remoção explícita da imagem
        if (Boolean.TRUE.equals(dto.removeCoverImage())) {
            news.setCoverImage(null);
        }
        // Upload de nova imagem (sobrescreve remoção se ambos vierem, nova imagem tem prioridade)
        if (dto.coverImage() != null && !dto.coverImage().isEmpty()) {
            String url = imgBBService.uploadImage(dto.coverImage(), ImageType.NEWS_COVER);
            news.setCoverImage(url);
        }

        return toDetailDTO(newsRepository.save(news));
    }

    @Transactional
    public void deleteNews(UUID id, UUID requesterId, boolean isAdmin) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada"));

        validateOwnership(news, requesterId, isAdmin);

        newsRepository.delete(news);
    }

    private void validateOwnership(News news, UUID requesterId, boolean isAdmin) {
        if (isAdmin) return;
        if (!news.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("Você só pode alterar notícias criadas por você.");
        }
    }

    private NewsDetailDTO toDetailDTO(News news) {
        return new NewsDetailDTO(
                news.getId(),
                news.getTitle(),
                news.getSlug(),
                news.getSummary(),
                news.getContent(),
                news.getCoverImage(),
                news.getPublishDate()
        );
    }

    private NewsSummaryDTO toSummary(NewsSummaryProjection projection) {
        return new NewsSummaryDTO(projection.id(), projection.title(), projection.slug(),
                projection.summary(), projection.coverImage(), projection.publishDate());
    }

    private NewsDetailDTO toDetail(NewsDetailProjection projection) {
        return new NewsDetailDTO(projection.id(), projection.title(), projection.slug(),
                projection.summary(), projection.content(), projection.coverImage(), projection.publishDate());
    }
}
