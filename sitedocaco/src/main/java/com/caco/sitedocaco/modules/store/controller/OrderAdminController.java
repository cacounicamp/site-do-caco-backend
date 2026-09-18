package com.caco.sitedocaco.modules.store.controller;

import com.caco.sitedocaco.modules.store.dto.response.OrderCsvDTO;
import com.caco.sitedocaco.modules.store.entity.OrderStatus;
import com.caco.sitedocaco.modules.store.entity.Order;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 30, refillTokens = 30)
public class OrderAdminController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }


    /**
     * Filtra pedidos por produto ou categoria
     * Ex: GET /api/admin/orders/filter?productId=xxx
     * Ex: GET /api/admin/orders/filter?categoryId=xxx
     * Ex: GET /api/admin/orders/filter?productId=xxx&status=PENDING
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Order>> filterOrders(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) OrderStatus status) {

        if (productId != null && categoryId != null) {
            return ResponseEntity.badRequest().build();
        }

        List<Order> orders;

        if (productId != null) {
            if (status != null) {
                orders = orderService.getOrdersByProductAndStatus(productId, status);
            } else {
                orders = orderService.getOrdersByProduct(productId);
            }
        } else if (categoryId != null) {
            if (status != null) {
                orders = orderService.getOrdersByCategoryAndStatus(categoryId, status);
            } else {
                orders = orderService.getOrdersByCategory(categoryId);
            }
        } else {
            // Se nenhum filtro for fornecido, retorna todos
            orders = orderService.getAllOrders();
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * Exporta pedidos em CSV
     * Ex: GET /api/admin/orders/export/csv?productId=xxx
     * Ex: GET /api/admin/orders/export/csv?categoryId=xxx
     * Ex: GET /api/admin/orders/export/csv?productId=xxx&status=PENDING
     */
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportOrdersToCsv(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) OrderStatus status) {

        if (productId != null && categoryId != null) {
            return ResponseEntity.badRequest().body("Escolha apenas um filtro: productId OU categoryId");
        }

        List<OrderCsvDTO> csvData;
        String filename;

        if (productId != null) {
            csvData = orderService.exportProductOrdersToCsv(productId, status);
            filename = "pedidos-produto-" + productId + ".csv";
        } else if (categoryId != null) {
            csvData = orderService.exportCategoryOrdersToCsv(categoryId, status);
            filename = "pedidos-categoria-" + categoryId + ".csv";
        } else {
            // Exporta todos os pedidos se nenhum filtro for fornecido
            List<Order> allOrders = orderService.getAllOrders();
            csvData = allOrders.stream()
                    .map(OrderCsvDTO::fromEntity)
                    .collect(Collectors.toList());
            filename = "todos-pedidos.csv";
        }

        String csvContent = orderService.convertToCsvString(csvData);

        if (csvContent.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }
}