package com.caco.sitedocaco.modules.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductDTO(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
        String slug,
        String description,
        @NotNull @Positive BigDecimal price,
        BigDecimal originalPrice,
        @NotNull UUID categoryId,
        Boolean manageStock,
        @Min(0) Integer stockQuantity,
        Boolean active
) {}