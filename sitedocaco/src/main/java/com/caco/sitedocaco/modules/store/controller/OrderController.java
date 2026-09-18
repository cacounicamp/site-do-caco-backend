package com.caco.sitedocaco.modules.store.controller;

import com.caco.sitedocaco.modules.store.dto.response.UserOrderDTO;
import com.caco.sitedocaco.modules.store.dto.response.UserOrderDetailDTO;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.store.entity.OrderStatus;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.store.service.OrderService;
import com.caco.sitedocaco.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
@RateLimit
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * Obtém todos os pedidos do usuário
     * Inclui tanto pedidos pagos (PENDING, DELIVERED) quanto não pagos (PENDING_PAYMENT)
     */
    @GetMapping
    public ResponseEntity<List<UserOrderDTO>> getMyOrders() {
        User user = userService.getCurrentUser();
        List<UserOrderDTO> orders = orderService.getUserOrders(user);
        return ResponseEntity.ok(orders);
    }

    /**
     * Obtém pedidos do usuário por status
     * PENDING_PAYMENT = Carrinho (não pagos)
     * PENDING = Pagos, aguardando retirada
     * DELIVERED = Entregues/retirados
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserOrderDTO>> getMyOrdersByStatus(@PathVariable OrderStatus status) {
        User user = userService.getCurrentUser();
        List<UserOrderDTO> orders = orderService.getUserOrdersByStatus(user, status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Obtém detalhes de um pedido específico do usuário
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<UserOrderDetailDTO> getMyOrderDetail(@PathVariable UUID orderId) {
        User user = userService.getCurrentUser();
        UserOrderDetailDTO order = orderService.getUserOrderDetail(orderId, user);
        return ResponseEntity.ok(order);
    }

    /**
     * Cria um novo pedido (adiciona ao carrinho)
     * Status inicial: PENDING_PAYMENT
     */
    @RateLimit(capacity = 10, refillTokens = 10, refillPeriod = 1)
    @PostMapping
    public ResponseEntity<UserOrderDTO> createOrder(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variationId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes) {

        User user = userService.getCurrentUser();

        // Usamos o serviço existente para criar o pedido
        var order = orderService.createOrder(user, productId, variationId, quantity, notes);

        // Converter para DTO
        UserOrderDTO orderDTO = UserOrderDTO.fromEntity(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    /**
     * Endpoint para obter apenas o carrinho do usuário (pedidos não pagos)
     * Alias para GET /api/user/orders/status/PENDING_PAYMENT
     */
    @GetMapping("/cart")
    public ResponseEntity<List<UserOrderDTO>> getMyCart() {
        User user = userService.getCurrentUser();
        List<UserOrderDTO> cartItems = orderService.getUserOrdersByStatus(user, OrderStatus.PENDING_PAYMENT);
        return ResponseEntity.ok(cartItems);
    }

    /**
     * Endpoint para obter pedidos pagos aguardando retirada
     * Alias para GET /api/user/orders/status/PENDING
     */
    @GetMapping("/awaiting-pickup")
    public ResponseEntity<List<UserOrderDTO>> getAwaitingPickup() {
        User user = userService.getCurrentUser();
        List<UserOrderDTO> orders = orderService.getUserOrdersByStatus(user, OrderStatus.PENDING);
        return ResponseEntity.ok(orders);
    }

    /**
     * Endpoint para obter histórico de pedidos entregues
     * Alias para GET /api/user/orders/status/DELIVERED
     */
    @GetMapping("/history")
    public ResponseEntity<List<UserOrderDTO>> getOrderHistory() {
        User user = userService.getCurrentUser();
        List<UserOrderDTO> orders = orderService.getUserOrdersByStatus(user, OrderStatus.DELIVERED);
        return ResponseEntity.ok(orders);
    }
}