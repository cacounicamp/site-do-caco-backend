package com.caco.sitedocaco.modules.stickers.repository;

import com.caco.sitedocaco.modules.stickers.entity.Sticker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface StickerRepository extends JpaRepository<Sticker, UUID> {
    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT s FROM Sticker s LEFT JOIN FETCH s.originEvent ORDER BY s.createdAt DESC")
    Page<Sticker> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

