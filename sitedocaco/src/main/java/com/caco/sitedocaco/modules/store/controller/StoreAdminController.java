package com.caco.sitedocaco.modules.store.controller;

import com.caco.sitedocaco.modules.store.dto.request.CreateProductCategoryDTO;
import com.caco.sitedocaco.modules.store.dto.request.CreateProductDTO;
import com.caco.sitedocaco.modules.store.dto.request.CreateProductVariationDTO;
import com.caco.sitedocaco.modules.store.dto.request.UpdateProductCategoryDTO;
import com.caco.sitedocaco.modules.store.dto.request.UpdateProductDTO;
import com.caco.sitedocaco.modules.store.dto.request.UpdateProductVariationDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductCategoryDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductDetailAdminDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductImageResponseDTO;
import com.caco.sitedocaco.modules.store.dto.response.ProductVariationDTO;
import com.caco.sitedocaco.modules.store.service.ProductCategoryService;
import com.caco.sitedocaco.modules.store.service.ProductService;
import com.caco.sitedocaco.modules.store.service.ProductVariationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/store")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 30, refillTokens = 30)
public class StoreAdminController {

    private final ProductCategoryService categoryService;
    private final ProductService productService;
    private final ProductVariationService variationService;

    // ========== CATEGORIAS ==========

    @GetMapping("/categories")
    public ResponseEntity<List<ProductCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ProductCategoryDTO> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<ProductCategoryDTO> createCategory(@RequestBody @Valid CreateProductCategoryDTO dto) {
        ProductCategoryDTO created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ProductCategoryDTO> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories/reorder")
    public ResponseEntity<Void> reorderCategories(@RequestBody List<UUID> categoryIds) {
        categoryService.reorderCategories(categoryIds);
        return ResponseEntity.ok().build();
    }

    // ========== PRODUTOS ==========

    @GetMapping("/products")
    public ResponseEntity<List<ProductDetailAdminDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<List<ProductDetailAdminDTO>> getProductsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDetailAdminDTO> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/products/slug/{slug}")
    public ResponseEntity<ProductDetailAdminDTO> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDetailAdminDTO> createProduct(
            @RequestBody @Valid CreateProductDTO dto) {
        ProductDetailAdminDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDetailAdminDTO> updateProduct(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductDTO dto) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products/{productId}/images/reorder")
    public ResponseEntity<Void> reorderProductImages(
            @PathVariable UUID productId,
            @RequestBody List<UUID> imageIds) {
        productService.reorderProductImages(productId, imageIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/{productId}/images")
    public ResponseEntity<List<ProductImageResponseDTO>> getProductImages(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductImages(productId));
    }

    @PostMapping("/products/{productId}/images")
    public ResponseEntity<ProductImageResponseDTO> uploadProductImage(
            @PathVariable UUID productId,
            @RequestParam("image") MultipartFile imageFile) throws IOException {

        ProductImageResponseDTO response = productService.addProductImage(productId, imageFile);
        return ResponseEntity.ok(response);
    }

    // Deletar uma imagem específica
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable UUID imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.noContent().build();
    }


    // ========== VARIAÇÕES ==========

    @GetMapping("/products/{productId}/variations")
    public ResponseEntity<List<ProductVariationDTO>> getProductVariations(@PathVariable UUID productId) {
        return ResponseEntity.ok(variationService.getVariationsByProduct(productId));
    }

    @GetMapping("/variations/{id}")
    public ResponseEntity<ProductVariationDTO> getVariationById(@PathVariable UUID id) {
        return ResponseEntity.ok(variationService.getVariationById(id));
    }

    @PostMapping("/products/{productId}/variations")
    public ResponseEntity<ProductVariationDTO> createVariation(
            @PathVariable UUID productId,
            @RequestBody @Valid CreateProductVariationDTO dto) {
        ProductVariationDTO created = variationService.createVariation(productId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/variations/{id}")
    public ResponseEntity<ProductVariationDTO> updateVariation(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductVariationDTO dto) {
        return ResponseEntity.ok(variationService.updateVariation(id, dto));
    }

    @DeleteMapping("/variations/{id}")
    public ResponseEntity<Void> deleteVariation(@PathVariable UUID id) {
        variationService.deleteVariation(id);
        return ResponseEntity.noContent().build();
    }
}