package com.caco.sitedocaco.modules.home.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdateBannerDTO(
        String title,
        MultipartFile imageFile,
        String targetLink,
        Boolean active
) {}