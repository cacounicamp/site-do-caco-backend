package com.caco.sitedocaco.modules.stickers.controller;

import com.caco.sitedocaco.modules.stickers.dto.request.CreateStickerDTO;
import com.caco.sitedocaco.modules.stickers.dto.request.GenerateRedemptionCodesDTO;
import com.caco.sitedocaco.modules.stickers.dto.request.UpdateStickerDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.RedemptionCodeBatchResponseDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.RedemptionCodeDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.StickerAdminDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.stickers.service.RedemptionCodeService;
import com.caco.sitedocaco.modules.stickers.service.StickerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/stickers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 30, refillTokens = 30)
public class StickerAdminController {

    private final StickerService stickerService;
    private final RedemptionCodeService redemptionCodeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StickerAdminDTO> create(
            @ModelAttribute @Valid CreateStickerDTO dto
    ) throws IOException {
        StickerAdminDTO created = stickerService.createSticker(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{stickerId}")
    public ResponseEntity<StickerAdminDTO> update(
            @PathVariable UUID stickerId,
            @ModelAttribute @Valid UpdateStickerDTO dto
    ) throws IOException {
        StickerAdminDTO updated = stickerService.updateSticker(stickerId, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{stickerId}/codes")
    public ResponseEntity<RedemptionCodeBatchResponseDTO> generateCodes(
            @PathVariable UUID stickerId,
            @RequestBody @Valid GenerateRedemptionCodesDTO dto
    ) {
        RedemptionCodeBatchResponseDTO resp = redemptionCodeService.generateBatch(stickerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{stickerId}/codes")
    public ResponseEntity<List<RedemptionCodeDTO>> getCodesForSticker(
            @PathVariable UUID stickerId
    ) {
        List<RedemptionCodeDTO> codes = redemptionCodeService.getCodesByStickerId(stickerId);
        return ResponseEntity.ok(codes);
    }
}
