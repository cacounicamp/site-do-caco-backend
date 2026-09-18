package com.caco.sitedocaco.modules.store.dto.response;

import com.caco.sitedocaco.modules.store.entity.Order;
import com.caco.sitedocaco.modules.store.entity.ProductVariation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCsvDTO(
        String orderNumber,
        LocalDateTime orderDate,
        String customerName,
        String customerEmail,
        String productName,
        String variationName,
        String notes,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        String mercadoPagoPaymentId,
        String status
) {
    public static OrderCsvDTO fromEntity(Order order) {
        String customerName = order.getUser().getUsername();
        String customerEmail = order.getUser().getEmail();
        String productName = order.getProduct().getName();

        ProductVariation variation = order.getVariation();
        String variationName = variation != null ? variation.getName() : "Nenhuma";

        String notes = order.getNotes() != null ? order.getNotes() : "";
        String paymentId = order.getMercadoPagoPaymentId() != null ? order.getMercadoPagoPaymentId() : "Pendente";

        return new OrderCsvDTO(
                order.getOrderNumber(),
                order.getCreatedAt(),
                customerName,
                customerEmail,
                productName,
                variationName,
                notes,
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotal(),
                paymentId,
                order.getStatus().name()
        );
    }
}