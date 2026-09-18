package com.caco.sitedocaco.modules.store.repository;

import com.caco.sitedocaco.modules.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // Admin: todos os produtos
    List<Product> findAllByOrderByCreatedAtDesc();

    // Público: produtos ativos por categoria ordenados por popularidade
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.slug = :categorySlug ORDER BY (SELECT COUNT(o.id) FROM Order o WHERE o.product = p) DESC")
    List<Product> findActiveProductsByCategoryOrderByPopularity(String categorySlug);

    // Admin: produtos por categoria
    List<Product> findByCategoryIdOrderByCreatedAtDesc(UUID categoryId);

    // Verificar se categoria tem produtos ativos
    long countByCategoryIdAndActiveTrue(UUID categoryId);
}