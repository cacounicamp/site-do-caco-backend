package com.caco.sitedocaco.modules.store.dto.response;

import com.caco.sitedocaco.modules.store.entity.OrderStatus;
import com.caco.sitedocaco.modules.store.entity.Order;
import com.caco.sitedocaco.modules.store.entity.Product;
import com.caco.sitedocaco.modules.store.entity.ProductCategory;
import com.caco.sitedocaco.modules.store.entity.ProductVariation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserOrderDetailDTO(
        UUID id,
        String orderNumber,

        // Informações do produto
        UUID productId,
        String productName,
        String productSlug,
        String productDescription,
        String productImage,

        // Informações da variação (se houver)
        UUID variationId,
        String variationName,
        BigDecimal variationAdditionalPrice,

        // Informações do pedido
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        OrderStatus status,

        // Datas
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        LocalDateTime deliveredAt,

        // Informações adicionais
        String notes,
        String mercadoPagoPreferenceId,
        String mercadoPagoPaymentId,

        // Informações da categoria (para navegação)
        UUID categoryId,
        String categoryName,
        String categorySlug
) {
    public static UserOrderDetailDTO fromEntity(Order order) {
        Product product = order.getProduct();
        ProductVariation variation = order.getVariation();
        ProductCategory category = product.getCategory();

        String productImage = product.getImages().isEmpty() ? null : product.getImages().getFirst().getImageUrl();

        return new UserOrderDetailDTO(
                order.getId(),
                order.getOrderNumber(),

                // Informações do produto
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                productImage,

                // Informações da variação
                variation != null ? variation.getId() : null,
                variation != null ? variation.getName() : null,
                variation != null ? variation.getAdditionalPrice() : null,

                // Informações do pedido
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotal(),
                order.getStatus(),

                // Datas
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getDeliveredAt(),

                // Informações adicionais
                order.getNotes(),
                order.getMercadoPagoPreferenceId(),
                order.getMercadoPagoPaymentId(),

                // Informações da categoria
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                category != null ? category.getSlug() : null
        );
    }
}