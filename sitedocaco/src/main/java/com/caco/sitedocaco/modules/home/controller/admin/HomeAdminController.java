package com.caco.sitedocaco.modules.home.controller.admin;

import com.caco.sitedocaco.modules.home.dto.request.CreateBannerDTO;
import com.caco.sitedocaco.modules.home.dto.request.CreateWarningDTO;
import com.caco.sitedocaco.modules.home.dto.request.ReorderBannersDTO;
import com.caco.sitedocaco.modules.home.dto.request.UpdateBannerDTO;
import com.caco.sitedocaco.modules.home.dto.request.UpdateWarningDTO;
import com.caco.sitedocaco.modules.home.dto.response.BannerDTO;
import com.caco.sitedocaco.modules.home.dto.response.WarningDTO;
import com.caco.sitedocaco.modules.home.entity.Banner;
import com.caco.sitedocaco.modules.home.entity.Warning;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.home.service.BannerService;
import com.caco.sitedocaco.modules.home.service.WarningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@RateLimit(capacity = 30, refillTokens = 30)
public class HomeAdminController {

    private final BannerService bannerService;
    private final WarningService warningService;

    // ==================== BANNERS (ADMIN ONLY) ====================

    @GetMapping("/banners/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BannerDTO>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    @GetMapping("/banners/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BannerDTO>> getInactiveBanners() {
        return ResponseEntity.ok(bannerService.getInactiveBanners());
    }

    @PostMapping(value = "/banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Banner> createBanner(@ModelAttribute @Valid CreateBannerDTO dto) throws IOException {
        return ResponseEntity.ok(bannerService.createBanner(dto));
    }

    @PutMapping(value = "/banners/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Banner> updateBanner(
            @PathVariable UUID id,
            @ModelAttribute @Valid UpdateBannerDTO dto) throws IOException {

        Banner updated = bannerService.updateBanner(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/banners/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Banner> toggleBannerActive(@PathVariable UUID id) {
        return ResponseEntity.ok(bannerService.toggleActive(id));
    }

    @PutMapping("/banners/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> reorderBanners(@RequestBody @Valid ReorderBannersDTO dto) {
        bannerService.reorderBanners(dto.bannerIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/banners/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBanner(@PathVariable UUID id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== WARNINGS (ADMIN ONLY) ====================

    @GetMapping("/warnings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<WarningDTO>> getAllWarnings() {
        return ResponseEntity.ok(warningService.getAllWarnings());
    }

    @PostMapping("/warnings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Warning> createWarning(@RequestBody @Valid CreateWarningDTO dto) {
        return ResponseEntity.ok(warningService.createWarning(dto));
    }

    @PutMapping("/warnings/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Warning> updateWarning(@PathVariable UUID id, @RequestBody @Valid UpdateWarningDTO dto) {
        return ResponseEntity.ok(warningService.updateWarning(id, dto));
    }

    @PutMapping("/warnings/{id}/expire")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> expireWarning(@PathVariable UUID id) {
        warningService.expireWarningNow(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/warnings/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteWarning(@PathVariable UUID id) {
        warningService.deleteWarning(id);
        return ResponseEntity.noContent().build();
    }
}
