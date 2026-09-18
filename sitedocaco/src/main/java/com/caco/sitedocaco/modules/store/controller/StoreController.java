package com.caco.sitedocaco.modules.store.controller;

import com.caco.sitedocaco.modules.store.dto.response.ProductCategoryDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductDetailDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductOverviewDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.store.service.ProductCategoryService;
import com.caco.sitedocaco.modules.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public/store")
@RequiredArgsConstructor
@RateLimit
public class StoreController {

    private final ProductCategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/categories")
    public ResponseEntity<List<ProductCategoryDTO>> getCategoriesWithActiveProducts() {
        return ResponseEntity.ok(categoryService.getCategoriesWithActiveProducts());
    }

    @GetMapping("/categories/{categorySlug}/products")
    public ResponseEntity<List<ProductOverviewDTO>> getActiveProductsByCategory(@PathVariable String categorySlug) {
        return ResponseEntity.ok(productService.getActiveProductsByCategory(categorySlug));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDetailDTO> getActiveProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getActiveProductById(id));
    }

    @GetMapping("/products/slug/{slug}")
    public ResponseEntity<ProductDetailDTO> getActiveProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getActiveProductBySlug(slug));
    }
}