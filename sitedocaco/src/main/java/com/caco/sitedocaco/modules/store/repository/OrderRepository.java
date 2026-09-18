package com.caco.sitedocaco.modules.store.repository;

import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.store.entity.OrderStatus;
import com.caco.sitedocaco.modules.store.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByMercadoPagoPaymentId(String paymentId);
    Optional<Order> findByMercadoPagoPreferenceId(String preferenceId);

    // Contar pedidos por produto (para ordenação)
    long countByProductId(UUID productId);

    // Novos métodos para filtro
    List<Order> findByProductId(UUID productId);

    @Query("SELECT o FROM Order o WHERE o.product.category.id = :categoryId")
    List<Order> findByProductCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT o FROM Order o WHERE o.product.category.id = :categoryId AND o.status = :status")
    List<Order> findByProductCategoryIdAndStatus(@Param("categoryId") UUID categoryId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.product.id = :productId AND o.status = :status")
    List<Order> findByProductIdAndStatus(@Param("productId") UUID productId, @Param("status") OrderStatus status);

    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);
}