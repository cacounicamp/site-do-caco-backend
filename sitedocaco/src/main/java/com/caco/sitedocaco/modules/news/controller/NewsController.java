package com.caco.sitedocaco.modules.news.controller;

import com.caco.sitedocaco.modules.news.dto.response.NewsDetailDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsSummaryDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/news")
@RequiredArgsConstructor
@RateLimit
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<Page<NewsSummaryDTO>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        return ResponseEntity.ok(newsService.getAllNews(pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<NewsDetailDTO> getNewsBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(newsService.getNewsBySlug(slug));
    }
}

