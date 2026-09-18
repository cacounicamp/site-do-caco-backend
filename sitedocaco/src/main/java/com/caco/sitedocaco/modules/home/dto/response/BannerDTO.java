package com.caco.sitedocaco.modules.home.dto.response;

import java.util.UUID;

public record BannerDTO(
        UUID id,
        String title,
        String imageUrl,
        String targetLink
) {}