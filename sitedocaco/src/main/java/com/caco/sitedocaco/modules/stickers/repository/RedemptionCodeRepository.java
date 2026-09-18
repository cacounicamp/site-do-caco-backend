package com.caco.sitedocaco.modules.stickers.repository;

import com.caco.sitedocaco.modules.stickers.entity.RedemptionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RedemptionCodeRepository extends JpaRepository<RedemptionCode, String> {
    boolean existsByCode(String code);

    List<RedemptionCode> findBySticker_Id(UUID stickerId);
}
