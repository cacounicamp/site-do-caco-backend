package com.caco.sitedocaco.features.home.dto.response;

import com.caco.sitedocaco.features.news.dto.response.NewsSummaryDTO;

import java.util.List;

public record DashboardDTO(
        List<BannerDTO> banners,
        List<WarningDTO> warnings,
        List<NewsSummaryDTO> latestNews
) {}
