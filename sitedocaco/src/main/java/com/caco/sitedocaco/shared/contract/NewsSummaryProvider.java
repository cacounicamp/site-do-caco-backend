package com.caco.sitedocaco.shared.contract;

import com.caco.sitedocaco.modules.news.dto.response.NewsSummaryDTO;

import java.util.List;

public interface NewsSummaryProvider {
    List<NewsSummaryDTO> getLatestNews(int limit);
}
