package com.caco.sitedocaco.modules.store.repository;

import com.caco.sitedocaco.modules.store.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(UUID productId);
    void deleteByProductId(UUID productId);
}