package com.caco.sitedocaco.features.stickers.service;

import com.caco.sitedocaco.features.stickers.dto.response.ClaimStickerResponseDTO;
import com.caco.sitedocaco.features.stickers.dto.response.MyStickerDTO;
import com.caco.sitedocaco.features.stickers.dto.response.StickerPublicDTO;
import com.caco.sitedocaco.features.users.entity.User;
import com.caco.sitedocaco.features.stickers.entity.RedemptionCode;
import com.caco.sitedocaco.features.stickers.entity.Sticker;
import com.caco.sitedocaco.features.stickers.entity.UserSticker;
import com.caco.sitedocaco.features.users.service.UserService;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.features.stickers.repository.RedemptionCodeRepository;
import com.caco.sitedocaco.features.stickers.repository.UserStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStickerService {

    private final UserService userService;
    private final RedemptionCodeRepository redemptionCodeRepository;
    private final UserStickerRepository userStickerRepository;

    @Transactional
    public ClaimStickerResponseDTO claim(String codeRaw) {
        String code = codeRaw == null ? null : codeRaw.trim().toUpperCase();
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("Código inválido.");
        }

        RedemptionCode redemptionCode = redemptionCodeRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Código de resgate não encontrado."));

        if (redemptionCode.getExpiresAt() != null && redemptionCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Código expirado.");
        }

        User user = userService.getCurrentUser();
        Sticker sticker = redemptionCode.getSticker();

        if (userStickerRepository.existsByUserIdAndStickerId(user.getId(), sticker.getId())) {
            throw new BusinessRuleException("Você já possui esse sticker.");
        }

        if (Boolean.TRUE.equals(redemptionCode.getIsOneTimeUse())) {
            if (Boolean.TRUE.equals(redemptionCode.getIsUsed())) {
                throw new BusinessRuleException("Esse código já foi usado.");
            }
            // marca usado dentro da mesma transação (com @Version na entidade)
            redemptionCode.setIsUsed(true);
            redemptionCode.setUsedAt(LocalDateTime.now());
            try {
                redemptionCodeRepository.save(redemptionCode);
            } catch (OptimisticLockingFailureException e) {
                throw new BusinessRuleException("Esse código acabou de ser usado. Tente outro.");
            }
        }

        UserSticker us = new UserSticker();
        us.setUser(user);
        us.setSticker(sticker);
        UserSticker saved = userStickerRepository.save(us);

        return new ClaimStickerResponseDTO(StickerPublicDTO.fromEntity(sticker), saved.getObtainedAt());
    }

    @Transactional(readOnly = true)
    public Page<MyStickerDTO> myStickers(Pageable pageable) {
        User user = userService.getCurrentUser();
        return userStickerRepository.findAllByUserIdOrderByObtainedAtDesc(user.getId(), pageable)
                .map(MyStickerDTO::fromEntity);
    }
}
