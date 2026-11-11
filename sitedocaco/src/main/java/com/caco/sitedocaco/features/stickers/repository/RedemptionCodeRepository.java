package com.caco.sitedocaco.features.stickers.repository;

import com.caco.sitedocaco.features.stickers.entity.RedemptionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RedemptionCodeRepository extends JpaRepository<RedemptionCode, String> {
    boolean existsByCode(String code);

    List<RedemptionCode> findBySticker_Id(UUID stickerId);
}
