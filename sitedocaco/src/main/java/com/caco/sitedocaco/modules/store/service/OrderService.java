package com.caco.sitedocaco.modules.store.service;

import com.caco.sitedocaco.modules.store.dto.request.CreateOrderDTO;
import com.caco.sitedocaco.modules.store.dto.response.OrderCsvDTO;
import com.caco.sitedocaco.modules.store.dto.response.UserOrderDTO;
import com.caco.sitedocaco.modules.store.dto.response.UserOrderDetailDTO;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.store.entity.OrderStatus;
import com.caco.sitedocaco.modules.store.entity.Order;
import com.caco.sitedocaco.modules.store.entity.Product;
import com.caco.sitedocaco.modules.store.entity.ProductVariation;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.store.repository.OrderRepository;
import com.caco.sitedocaco.modules.store.repository.ProductRepository;
import com.caco.sitedocaco.modules.store.repository.ProductVariationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository variationRepository;

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<UserOrderDTO> getUserOrdersDTO(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(UserOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserOrderDTO> getUserOrdersByStatusDTO(User user, OrderStatus status) {
        return orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status).stream()
                .map(UserOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserOrderDetailDTO getUserOrderDetailDTO(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Pedido não encontrado");
        }

        return UserOrderDetailDTO.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    @Transactional
    public Order createOrder(User user, CreateOrderDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getActive()) {
            throw new BusinessRuleException("Produto não está disponível");
        }

        ProductVariation variation = null;
        if (dto.variationId() != null) {
            variation = variationRepository.findById(dto.variationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variação não encontrada"));
        }

        checkStock(product, variation, dto.quantity());

        BigDecimal unitPrice = product.getPrice();
        if (variation != null && variation.getAdditionalPrice() != null) {
            unitPrice = unitPrice.add(variation.getAdditionalPrice());
        }

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setVariation(variation);
        order.setQuantity(dto.quantity());
        order.setUnitPrice(unitPrice);
        order.setNotes(dto.notes());
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT && status == OrderStatus.PENDING) {
            checkStock(order.getProduct(), order.getVariation(), order.getQuantity());
            reduceStock(order.getProduct(), order.getVariation(), order.getQuantity());
            order.setPaidAt(LocalDateTime.now());
        } else if (order.getStatus() == OrderStatus.PENDING && status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        } else {
            throw new BusinessRuleException("Transição de status não permitida: de " + order.getStatus() + " para " + status);
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByProduct(UUID productId) {
        return orderRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCategory(UUID categoryId) {
        return orderRepository.findByProductCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByProductAndStatus(UUID productId, OrderStatus status) {
        return orderRepository.findByProductIdAndStatus(productId, status);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCategoryAndStatus(UUID categoryId, OrderStatus status) {
        return orderRepository.findByProductCategoryIdAndStatus(categoryId, status);
    }

    @Transactional(readOnly = true)
    public List<UserOrderDTO> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(UserOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserOrderDTO> getUserOrdersByStatus(User user, OrderStatus status) {
        return orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status).stream()
                .map(UserOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserOrderDetailDTO getUserOrderDetail(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Pedido não encontrado");
        }

        return UserOrderDetailDTO.fromEntity(order);
    }

    @Transactional
    public Order createOrder(User user, UUID productId, UUID variationId, Integer quantity, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getActive()) {
            throw new BusinessRuleException("Produto não está disponível");
        }

        ProductVariation variation = null;
        if (variationId != null) {
            variation = variationRepository.findById(variationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Variação não encontrada"));
        }

        checkStock(product, variation, quantity);

        BigDecimal unitPrice = product.getPrice();
        if (variation != null && variation.getAdditionalPrice() != null) {
            unitPrice = unitPrice.add(variation.getAdditionalPrice());
        }

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setVariation(variation);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setNotes(notes);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        return orderRepository.save(order);
    }


    @Transactional(readOnly = true)
    public List<OrderCsvDTO> exportProductOrdersToCsv(UUID productId, OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = getOrdersByProductAndStatus(productId, status);
        } else {
            orders = getOrdersByProduct(productId);
        }

        return orders.stream()
                .map(OrderCsvDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderCsvDTO> exportCategoryOrdersToCsv(UUID categoryId, OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = getOrdersByCategoryAndStatus(categoryId, status);
        } else {
            orders = getOrdersByCategory(categoryId);
        }

        return orders.stream()
                .map(OrderCsvDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String convertToCsvString(List<OrderCsvDTO> orders) {
        if (orders.isEmpty()) {
            return "";
        }

        StringBuilder csv = new StringBuilder();

        // Cabeçalho
        csv.append("Número do Pedido,Data do Pedido,Nome do Cliente,Email do Cliente,Nome do Produto,Variação,Observações,Quantidade,Preço Unitário,Total,ID do Pagamento (Mercado Pago),Status\n");

        // Dados
        for (OrderCsvDTO order : orders) {
            csv.append(escapeCsv(order.orderNumber())).append(",");
            csv.append(escapeCsv(order.orderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))).append(",");
            csv.append(escapeCsv(order.customerName())).append(",");
            csv.append(escapeCsv(order.customerEmail())).append(",");
            csv.append(escapeCsv(order.productName())).append(",");
            csv.append(escapeCsv(order.variationName())).append(",");
            csv.append(escapeCsv(order.notes())).append(",");
            csv.append(order.quantity()).append(",");
            csv.append(order.unitPrice()).append(",");
            csv.append(order.total()).append(",");
            csv.append(escapeCsv(order.mercadoPagoPaymentId())).append(",");
            csv.append(escapeCsv(order.status())).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void checkStock(Product product, ProductVariation variation, Integer quantity) {
        if (product.getManageStock()) {
            int availableStock;

            if (variation != null) {
                availableStock = variation.getStockQuantity() != null ? variation.getStockQuantity() : 0;
            } else {
                availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            }

            if (availableStock < quantity) {
                throw new BusinessRuleException("Estoque insuficiente");
            }
        }
    }

    private void reduceStock(Product product, ProductVariation variation, Integer quantity) {
        if (product.getManageStock()) {
            if (variation != null) {
                variation.setStockQuantity(variation.getStockQuantity() - quantity);
                variationRepository.save(variation);
            } else {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
            }
        }
    }
}