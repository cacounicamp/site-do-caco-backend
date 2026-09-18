package com.caco.sitedocaco.modules.home.repository;

import com.caco.sitedocaco.modules.home.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {
    // Busca apenas ativos, ordenados pela ordem de exibição definida no admin
    List<Banner> findByActiveTrueOrderByDisplayOrderAsc();
    List<Banner> findAllByActiveFalseOrderByTitleAsc();

    // Busca o maior valor de ordem para inserir o próximo no final
    @Query("SELECT MAX(b.displayOrder) FROM Banner b")
    Integer findMaxDisplayOrder();
}