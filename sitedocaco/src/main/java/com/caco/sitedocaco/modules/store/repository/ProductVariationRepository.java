package com.caco.sitedocaco.modules.store.repository;

import com.caco.sitedocaco.modules.store.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {
    List<ProductVariation> findByProductId(UUID productId);
    void deleteByProductId(UUID productId);
}