package com.caco.sitedocaco.modules.store.dto.response;

import com.caco.sitedocaco.modules.store.entity.OrderStatus;
import com.caco.sitedocaco.modules.store.entity.Order;
import com.caco.sitedocaco.modules.store.entity.Product;
import com.caco.sitedocaco.modules.store.entity.ProductVariation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserOrderDTO(
        UUID id,
        String orderNumber,
        String productName,
        String productSlug,
        String productImage,
        String variationName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        LocalDateTime deliveredAt,
        String notes,
        String mercadoPagoPreferenceId
) {
    public static UserOrderDTO fromEntity(Order order) {
        Product product = order.getProduct();
        ProductVariation variation = order.getVariation();

        String productImage = product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl();
        String variationName = variation != null ? variation.getName() : null;

        return new UserOrderDTO(
                order.getId(),
                order.getOrderNumber(),
                product.getName(),
                product.getSlug(),
                productImage,
                variationName,
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getDeliveredAt(),
                order.getNotes(),
                order.getMercadoPagoPreferenceId()
        );
    }
}