package com.caco.sitedocaco.modules.stickers.service;

import com.caco.sitedocaco.modules.stickers.dto.request.CreateStickerDTO;
import com.caco.sitedocaco.modules.stickers.dto.request.UpdateStickerDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.StickerAdminDTO;
import com.caco.sitedocaco.modules.stickers.dto.response.StickerPublicDTO;
import com.caco.sitedocaco.modules.events.entity.Event;
import com.caco.sitedocaco.modules.stickers.entity.Sticker;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.shared.contract.EventReference;
import com.caco.sitedocaco.modules.stickers.repository.StickerRepository;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StickerService {

    private final StickerRepository stickerRepository;
    private final EventReference eventReference;
    private final ImgBBService imgBBService;

    @Transactional
    public StickerAdminDTO createSticker(CreateStickerDTO dto) throws IOException {
        if (stickerRepository.existsByNameIgnoreCase(dto.name())) {
            throw new BusinessRuleException("Já existe um sticker com esse nome.");
        }

        if (dto.image() == null || dto.image().isEmpty()) {
            throw new BusinessRuleException("Imagem é obrigatória.");
        }

        // Faz upload e valida via ImgBBService (usa ImageType específico de adesivo se existir)
        String imageUrl = imgBBService.uploadImage(dto.image(), ImageType.PRODUCT_GALLERY);

        Sticker sticker = new Sticker();
        sticker.setName(dto.name().trim());
        sticker.setDescription(dto.description());
        sticker.setImageUrl(imageUrl);

        if (dto.originEventId() != null) {
            Event event = eventReference.getEvent(dto.originEventId());
            sticker.setOriginEvent(event);
        }

        Sticker saved = stickerRepository.save(sticker);
        return StickerAdminDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<StickerPublicDTO> listPublic(Pageable pageable) {
        return stickerRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(StickerPublicDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Sticker getStickerEntity(UUID stickerId) {
        return stickerRepository.findById(stickerId)
                .orElseThrow(() -> new ResourceNotFoundException("Sticker não encontrado."));
    }

    @Transactional
    public StickerAdminDTO updateSticker(UUID stickerId, UpdateStickerDTO dto) throws IOException {
        Sticker sticker = stickerRepository.findById(stickerId)
                .orElseThrow(() -> new ResourceNotFoundException("Sticker não encontrado."));

        // Verifica se o novo nome já existe (ignorando o próprio sticker)
        if (!sticker.getName().equalsIgnoreCase(dto.name()) &&
            stickerRepository.existsByNameIgnoreCase(dto.name())) {
            throw new BusinessRuleException("Já existe um sticker com esse nome.");
        }

        sticker.setName(dto.name().trim());
        sticker.setDescription(dto.description());

        // Se uma nova imagem foi fornecida, faz o upload
        if (dto.image() != null && !dto.image().isEmpty()) {
            String imageUrl = imgBBService.uploadImage(dto.image(), ImageType.PRODUCT_GALLERY);
            sticker.setImageUrl(imageUrl);
        }

        // Atualiza o evento de origem
        if (dto.originEventId() != null) {
            Event event = eventReference.getEvent(dto.originEventId());
            sticker.setOriginEvent(event);
        } else {
            sticker.setOriginEvent(null);
        }

        Sticker updated = stickerRepository.save(sticker);
        return StickerAdminDTO.fromEntity(updated);
    }
}
