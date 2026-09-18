package com.caco.sitedocaco.modules.home.dto.response;

import com.caco.sitedocaco.modules.home.dto.response.BannerDTO;
import com.caco.sitedocaco.modules.home.dto.response.WarningDTO;
import com.caco.sitedocaco.modules.news.dto.response.NewsSummaryDTO;

import java.util.List;

public record DashboardDTO(
        List<BannerDTO> banners,
        List<WarningDTO> warnings,
        List<NewsSummaryDTO> latestNews
) {}
