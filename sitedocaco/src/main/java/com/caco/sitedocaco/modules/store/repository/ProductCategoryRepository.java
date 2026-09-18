package com.caco.sitedocaco.modules.store.repository;

import com.caco.sitedocaco.modules.store.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    Optional<ProductCategory> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // Admin: todas as categorias ordenadas
    List<ProductCategory> findAllByOrderByOrderAsc();

    // Público: categorias com produtos ativos
    @Query("SELECT DISTINCT c FROM ProductCategory c JOIN Product p ON p.category = c WHERE p.active = true ORDER BY c.order ASC")
    List<ProductCategory> findCategoriesWithActiveProducts();

    @Query("SELECT MAX(c.order) FROM ProductCategory c")
    Integer findMaxOrder();
}