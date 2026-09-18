package com.caco.sitedocaco.modules.manual.repository;

import com.caco.sitedocaco.modules.manual.entity.ManualCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManualCategoryRepository extends JpaRepository<ManualCategory, UUID> {
    Optional<ManualCategory> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<ManualCategory> findAllByOrderByOrderAsc();

    @Query("SELECT MAX(c.order) FROM ManualCategory c")
    Integer findMaxOrder();
}