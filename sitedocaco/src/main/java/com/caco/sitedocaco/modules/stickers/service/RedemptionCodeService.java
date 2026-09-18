package com.caco.sitedocaco.modules.stickers.service;

import com.caco.sitedocaco.modules.stickers.dto.request.GenerateRedemptionCodesDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.RedemptionCodeBatchResponseDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.RedemptionCodeDTO;
import com.caco.sitedocaco.modules.stickers.entity.RedemptionCode;
import com.caco.sitedocaco.modules.stickers.entity.Sticker;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.modules.stickers.repository.RedemptionCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedemptionCodeService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem 0/O/I/1
    private static final int DEFAULT_CODE_LEN = 10;

    private final SecureRandom random = new SecureRandom();

    private final StickerService stickerService;
    private final RedemptionCodeRepository redemptionCodeRepository;

    @Transactional
    public RedemptionCodeBatchResponseDTO generateBatch(UUID stickerId, GenerateRedemptionCodesDTO dto) {
        if (dto.expiresAt() != null && dto.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("expiresAt não pode estar no passado.");
        }

        Sticker sticker = stickerService.getStickerEntity(stickerId);

        boolean oneTimeUse = dto.oneTimeUse() == null || dto.oneTimeUse();
        int quantity = dto.quantity();

        // Evita loop infinito se o banco tiver colisões (muito improvável)
        int maxAttempts = quantity * 20;

        Set<String> codes = new HashSet<>(quantity);
        int attempts = 0;
        while (codes.size() < quantity) {
            if (attempts++ > maxAttempts) {
                throw new BusinessRuleException("Não foi possível gerar códigos únicos. Tente novamente.");
            }

            String code = randomCode();
            if (codes.contains(code)) continue;
            if (redemptionCodeRepository.existsById(code)) continue;
            codes.add(code);
        }

        List<RedemptionCode> entities = new ArrayList<>(quantity);
        for (String code : codes) {
            RedemptionCode rc = new RedemptionCode();
            rc.setCode(code);
            rc.setSticker(sticker);
            rc.setIsOneTimeUse(oneTimeUse);
            rc.setIsUsed(false);
            rc.setExpiresAt(dto.expiresAt());
            entities.add(rc);
        }

        redemptionCodeRepository.saveAll(entities);
        return new RedemptionCodeBatchResponseDTO(quantity, new ArrayList<>(codes));
    }

    @Transactional(readOnly = true)
    public List<RedemptionCodeDTO> getCodesByStickerId(UUID stickerId) {
        // valida existência do sticker e evita retornar lista para um id inválido
        stickerService.getStickerEntity(stickerId);

        return redemptionCodeRepository.findBySticker_Id(stickerId).stream()
                .sorted(Comparator.comparing(RedemptionCode::getCode))
                .map(RedemptionCodeDTO::fromEntity)
                .toList();
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(RedemptionCodeService.DEFAULT_CODE_LEN);
        for (int i = 0; i < RedemptionCodeService.DEFAULT_CODE_LEN; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
