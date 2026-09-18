package com.caco.sitedocaco.modules.stickers.controller;

import com.caco.sitedocaco.modules.stickers.dto.request.ClaimStickerDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.ClaimStickerResponseDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.MyStickerDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.stickers.service.UserStickerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/stickers")
@RequiredArgsConstructor
@RateLimit
public class UserStickerController {

    private final UserStickerService userStickerService;

    // Resgate de sticker por código: limite estrito para evitar brute-force de códigos
    @RateLimit(capacity = 5, refillTokens = 5, refillPeriod = 1)
    @PostMapping("/claim")
    public ResponseEntity<ClaimStickerResponseDTO> claim(@RequestBody @Valid ClaimStickerDTO dto) {
        return ResponseEntity.ok(userStickerService.claim(dto.code()));
    }

    @GetMapping
    public ResponseEntity<Page<MyStickerDTO>> myStickers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userStickerService.myStickers(pageable));
    }
}
