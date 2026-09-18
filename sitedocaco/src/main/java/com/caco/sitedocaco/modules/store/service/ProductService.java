package com.caco.sitedocaco.modules.store.service;

import com.caco.sitedocaco.modules.store.dto.request.CreateProductDTO;
import com.caco.sitedocaco.modules.store.dto.request.UpdateProductDTO;
import com.caco.sitedocaco.modules.store.dto.response.*;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.modules.store.entity.Product;
import com.caco.sitedocaco.modules.store.entity.ProductCategory;
import com.caco.sitedocaco.modules.store.entity.ProductImage;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import com.caco.sitedocaco.modules.store.repository.ProductImageRepository;
import com.caco.sitedocaco.modules.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryService categoryService;
    private final ProductImageRepository productImageRepository;
    private final ImgBBService imgBBService;

    // Admin: todos os produtos
    public List<ProductDetailAdminDTO> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDetailAdminDTO)
                .toList();
    }

    // Admin: produtos por categoria
    public List<ProductDetailAdminDTO> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId).stream()
                .map(this::toDetailAdminDTO)
                .toList();
    }

    // Público: produtos ativos por categoria (ordenados por popularidade)
    public List<ProductOverviewDTO> getActiveProductsByCategory(String categorySlug) {
        List<Product> products = productRepository.findActiveProductsByCategoryOrderByPopularity(categorySlug);
        return products.stream()
                .map(this::toOverviewDTO)
                .toList();
    }

    // Admin: detalhes do produto
    public ProductDetailAdminDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        return toDetailAdminDTO(product);
    }

    // Público: detalhes do produto ativo
    public ProductDetailDTO getActiveProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getActive()) {
            throw new ResourceNotFoundException("Produto não encontrado");
        }

        return toDetailDTO(product);
    }

    // Por slug (público)
    public ProductDetailDTO getActiveProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getActive()) {
            throw new ResourceNotFoundException("Produto não encontrado");
        }

        return toDetailDTO(product);
    }

    // Por slug (admin)
    public ProductDetailAdminDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        return toDetailAdminDTO(product);
    }

    public ProductDetailAdminDTO createProduct(CreateProductDTO dto) {
        if (productRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe um produto com este slug");
        }

        Product product = new Product();
        product.setName(dto.name());
        product.setSlug(dto.slug());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setOriginalPrice(dto.originalPrice());
        product.setActive(dto.active() != null ? dto.active() : true);
        product.setManageStock(dto.manageStock() != null ? dto.manageStock() : true);
        product.setStockQuantity(dto.stockQuantity() != null ? dto.stockQuantity() : 0);

        ProductCategory category = categoryService.getCategoryEntityById(dto.categoryId());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return toDetailAdminDTO(savedProduct);
    }

    @Transactional
    public List<ProductImageResponseDTO> getProductImages(UUID productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        return images.stream()
                .map(img -> new ProductImageResponseDTO(
                        img.getId(),
                        img.getImageUrl(),
                        img.getDisplayOrder(),
                        productId
                ))
                .toList();
    }

    @Transactional
    public ProductImageResponseDTO addProductImage(UUID productId, MultipartFile imageFile) throws IOException {
        // 1. Buscar produto
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        // 2. Fazer upload da imagem
        String imageUrl = imgBBService.uploadImage(imageFile, ImageType.PRODUCT_GALLERY);

        // 3. Criar entidade ProductImage
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(imageUrl);

        // 4. Definir ordem (última posição)
        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        int newOrder = existingImages.isEmpty() ? 0 : existingImages.getLast().getDisplayOrder() + 1;
        productImage.setDisplayOrder(newOrder);

        // 5. Salvar
        ProductImage savedImage = productImageRepository.save(productImage);

        // 6. Retornar DTO
        return new ProductImageResponseDTO(
                savedImage.getId(),
                savedImage.getImageUrl(),
                savedImage.getDisplayOrder(),
                productId
        );
    }

    @Transactional
    public void deleteProductImage(UUID imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada"));

        productImageRepository.delete(image);
    }

    public ProductDetailAdminDTO updateProduct(UUID id, UpdateProductDTO dto) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (dto.name() != null && !dto.name().isBlank()) {
            product.setName(dto.name());
        }

        if (dto.slug() != null && !dto.slug().isBlank() && !dto.slug().equals(product.getSlug())) {
            if (productRepository.existsBySlug(dto.slug())) {
                throw new BusinessRuleException("Já existe um produto com este slug");
            }
            product.setSlug(dto.slug());
        }

        if (dto.description() != null) {
            product.setDescription(dto.description());
        }

        if (dto.price() != null) {
            product.setPrice(dto.price());
        }

        if (dto.originalPrice() != null) {
            product.setOriginalPrice(dto.originalPrice());
        }

        if (dto.active() != null) {
            product.setActive(dto.active());
        }

        if (dto.manageStock() != null) {
            product.setManageStock(dto.manageStock());
        }

        if (dto.stockQuantity() != null) {
            product.setStockQuantity(dto.stockQuantity());
        }

        if (dto.categoryId() != null) {
            ProductCategory category = categoryService.getCategoryEntityById(dto.categoryId());
            product.setCategory(category);
        }

        // Se novas imagens forem fornecidas, substituir as antigas
        if (dto.images() != null && !dto.images().isEmpty()) {
            // Limpar imagens antigas
            product.getImages().clear();

            // Adicionar novas imagens
            for (int i = 0; i < dto.images().size(); i++) {
                MultipartFile file = dto.images().get(i);
                if (!file.isEmpty()) {
                    String imageUrl = imgBBService.uploadImage(file, ImageType.PRODUCT_GALLERY);
                    ProductImage productImage = new ProductImage();
                    productImage.setProduct(product);
                    productImage.setImageUrl(imageUrl);
                    productImage.setDisplayOrder(i);
                    product.getImages().add(productImage);
                }
            }
        }

        Product savedProduct = productRepository.save(product);
        return toDetailAdminDTO(savedProduct);
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        productRepository.delete(product);
    }

    public void reorderProductImages(UUID productId, List<UUID> imageIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        // Verificar se todas as imagens pertencem ao produto
        List<UUID> productImageIds = product.getImages().stream()
                .map(ProductImage::getId)
                .toList();

        for (UUID imageId : imageIds) {
            if (!productImageIds.contains(imageId)) {
                throw new BusinessRuleException("A imagem " + imageId + " não pertence ao produto");
            }
        }

        // Reordenar
        for (int i = 0; i < imageIds.size(); i++) {
            UUID imageId = imageIds.get(i);
            int finalI = i;
            product.getImages().stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .ifPresent(img -> img.setDisplayOrder(finalI));
        }

        productRepository.save(product);
    }

    private ProductOverviewDTO toOverviewDTO(Product product) {
        String coverImage = product.getImages().isEmpty() ?
                null : product.getImages().getFirst().getImageUrl();

        boolean outOfStock = product.getManageStock() && product.getStockQuantity() <= 0;

        return new ProductOverviewDTO(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                coverImage,
                outOfStock,
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.getCreatedAt()
        );
    }

    private ProductDetailDTO toDetailDTO(Product product) {
        List<String> images = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getDisplayOrder))
                .map(ProductImage::getImageUrl)
                .toList();

        boolean outOfStock = product.getManageStock() && product.getStockQuantity() <= 0;

        List<ProductVariationDTO> variations = product.getVariations().stream()
                .map(var -> new ProductVariationDTO(
                        var.getId(),
                        var.getName(),
                        var.getAdditionalPrice(),
                        !product.getManageStock() || var.getStockQuantity() > 0
                ))
                .toList();

        return new ProductDetailDTO(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getManageStock(),
                outOfStock,
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                images,
                variations
        );
    }

    private ProductDetailAdminDTO toDetailAdminDTO(Product product) {
        List<String> images = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getDisplayOrder))
                .map(ProductImage::getImageUrl)
                .toList();

        List<ProductVariationDTO> variations = product.getVariations().stream()
                .map(var -> new ProductVariationDTO(
                        var.getId(),
                        var.getName(),
                        var.getAdditionalPrice(),
                        !product.getManageStock() || var.getStockQuantity() > 0
                ))
                .toList();

        return new ProductDetailAdminDTO(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getManageStock(),
                product.getStockQuantity(),
                product.getActive(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                images,
                variations,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
