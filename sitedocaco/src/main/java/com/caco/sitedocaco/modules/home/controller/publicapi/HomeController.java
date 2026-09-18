package com.caco.sitedocaco.modules.home.controller.publicapi;

import com.caco.sitedocaco.modules.home.dto.response.BannerDTO;
import com.caco.sitedocaco.modules.home.dto.response.DashboardDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsSummaryDTO;
import com.caco.sitedocaco.modules.home.dto.response.WarningDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.home.service.BannerService;
import com.caco.sitedocaco.shared.contract.NewsSummaryProvider;
import com.caco.sitedocaco.modules.home.service.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/home")
@RequiredArgsConstructor
@RateLimit
public class HomeController {

    private final BannerService bannerService;
    private final WarningService warningService;
    private final NewsSummaryProvider newsSummaryProvider;

    @GetMapping
    public ResponseEntity<DashboardDTO> getHomeDashboard() {
        // 1. Busca banners ativos (ordenados por prioridade)
        List<BannerDTO> banners = bannerService.getActiveBanners();

        // 2. Busca avisos urgentes (que não expiraram)
        List<WarningDTO> warnings = warningService.getActiveWarnings();

        // 3. Busca apenas as 3 últimas notícias para a capa
        List<NewsSummaryDTO> latestNews = newsSummaryProvider.getLatestNews(3);

        // 4. Monta o objeto de resposta agregado
        DashboardDTO dashboard = new DashboardDTO(
                banners,
                warnings,
                latestNews
        );

        return ResponseEntity.ok(dashboard);
    }
}
