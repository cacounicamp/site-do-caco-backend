package com.caco.sitedocaco.modules.home.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record CreateBannerDTO(
        @NotBlank String title,
        MultipartFile imageFile,
        @NotBlank String targetLink,
        Boolean active
) {}
