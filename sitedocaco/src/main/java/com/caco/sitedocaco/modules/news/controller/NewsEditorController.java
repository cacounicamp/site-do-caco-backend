package com.caco.sitedocaco.modules.news.controller;

import com.caco.sitedocaco.modules.news.dto.request.CreateNewsDTO;
import com.caco.sitedocaco.modules.news.dto.request.UpdateNewsDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsDetailDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsSummaryDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.news.service.NewsService;
import com.caco.sitedocaco.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/editor/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EDITOR')")
@RateLimit(capacity = 20, refillTokens = 20)
public class NewsEditorController {

    private final NewsService newsService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<NewsSummaryDTO>> getMyNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = userService.getCurrentUser().getId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        return ResponseEntity.ok(newsService.getNewsByAuthor(userId, pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<NewsDetailDTO> getMyNewsBySlug(@PathVariable String slug) {
        UUID userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(newsService.getNewsBySlugAndAuthor(slug, userId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDetailDTO> createNews(
            @ModelAttribute @Valid CreateNewsDTO dto) throws IOException {
        UUID userId = userService.getCurrentUser().getId();
        NewsDetailDTO created = newsService.createNews(dto, userId);
        return ResponseEntity.created(URI.create("/public/news/" + created.slug())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDetailDTO> updateNews(
            @PathVariable UUID id,
            @ModelAttribute UpdateNewsDTO dto) throws IOException {
        UUID userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(newsService.updateNews(id, dto, userId, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable UUID id) {
        UUID userId = userService.getCurrentUser().getId();
        newsService.deleteNews(id, userId, false);
        return ResponseEntity.noContent().build();
    }
}
