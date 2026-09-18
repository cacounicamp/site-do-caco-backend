package com.caco.sitedocaco.modules.stickers.controller;

import com.caco.sitedocaco.modules.stickers.dto.response.StickerPublicDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.stickers.service.StickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/stickers")
@RequiredArgsConstructor
@RateLimit
public class StickerController {

    private final StickerService stickerService;

    @GetMapping
    public ResponseEntity<Page<StickerPublicDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(stickerService.listPublic(pageable));
    }
}

