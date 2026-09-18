package com.caco.sitedocaco.modules.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductDTO(
        String name,
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
        String slug,
        String description,
        @Positive BigDecimal price,
        BigDecimal originalPrice,
        UUID categoryId,
        Boolean manageStock,
        @Min(0) Integer stockQuantity,
        Boolean active,
        List<MultipartFile> images
) {}