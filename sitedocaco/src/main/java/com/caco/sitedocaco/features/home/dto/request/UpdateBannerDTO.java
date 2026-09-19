package com.caco.sitedocaco.features.home.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdateBannerDTO(
        String title,
        MultipartFile imageFile,
        String targetLink,
        Boolean active
) {}