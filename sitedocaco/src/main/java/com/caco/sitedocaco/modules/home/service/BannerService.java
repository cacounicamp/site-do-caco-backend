package com.caco.sitedocaco.modules.home.service;

import com.caco.sitedocaco.modules.home.dto.request.CreateBannerDTO;
import com.caco.sitedocaco.modules.home.dto.request.UpdateBannerDTO;
import com.caco.sitedocaco.modules.home.dto.response.BannerDTO;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import com.caco.sitedocaco.modules.home.entity.Banner;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.home.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final ImgBBService imgBBService;

    @Transactional(readOnly = true)
    public List<BannerDTO> getActiveBanners() {
        return bannerRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(b -> new BannerDTO(b.getId(), b.getTitle(), b.getImageUrl(), b.getTargetLink()))
                .toList();
    }

    public List<BannerDTO> getInactiveBanners() {
        return bannerRepository.findAllByActiveFalseOrderByTitleAsc()
                .stream()
                .map(b -> new BannerDTO(b.getId(), b.getTitle(), b.getImageUrl(), b.getTargetLink()))
                .toList();
    }

    @Transactional
    public Banner createBanner(CreateBannerDTO dto) throws IOException {
        Banner banner = new Banner();
        banner.setTitle(dto.title());

        // Fazer upload da imagem
        if (dto.imageFile() != null && !dto.imageFile().isEmpty()) {
            String imageUrl = imgBBService.uploadImage(dto.imageFile(), ImageType.BANNER_IMAGE);
            banner.setImageUrl(imageUrl);
        }

        banner.setTargetLink(dto.targetLink());
        banner.setActive(dto.active() != null ? dto.active() : true);

        // Ordenação automática
        Integer maxOrder = bannerRepository.findMaxDisplayOrder();
        banner.setDisplayOrder(maxOrder == null ? 0 : maxOrder + 1);

        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner updateBanner(UUID id, UpdateBannerDTO dto) throws IOException {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner não encontrado"));

        if (dto.title() != null) banner.setTitle(dto.title());

        // Atualizar imagem se fornecida
        if (dto.imageFile() != null && !dto.imageFile().isEmpty()) {
            String imageUrl = imgBBService.uploadImage(dto.imageFile(), ImageType.BANNER_IMAGE);
            banner.setImageUrl(imageUrl);
        }

        if (dto.targetLink() != null) banner.setTargetLink(dto.targetLink());
        if (dto.active() != null) banner.setActive(dto.active());

        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner toggleActive(UUID id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner não encontrado"));
        banner.setActive(!banner.getActive());
        // Colocar banner em último na ordem se ativado
        if (banner.getActive()) {
            Integer maxOrder = bannerRepository.findMaxDisplayOrder();
            banner.setDisplayOrder(maxOrder == null ? 0 : maxOrder + 1);
        }
        return bannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(UUID id) {
        bannerRepository.deleteById(id);
    }

    /**
     * Função Especial: Reordenar Banners
     * Recebe uma lista de IDs na ordem desejada (A, D, B, C, E)
     * Itera e atualiza o displayOrder de acordo com o índice na lista.
     */
    @Transactional
    public void reorderBanners(List<UUID> orderedIds) {
        // Atualiza a ordem baseada na lista recebida.
        // Nota: Isso funciona mesmo se a lista misturar ativos e inativos,
        // mas idealmente o front deve mandar a lista do contexto que está vendo.
        for (int i = 0; i < orderedIds.size(); i++) {
            UUID id = orderedIds.get(i);
            // Usamos ifPresent para evitar erro caso o ID tenha sido deletado recentemente
            int finalI = i;
            bannerRepository.findById(id).ifPresent(banner -> {
                banner.setDisplayOrder(finalI);
                bannerRepository.save(banner);
            });
        }
    }
}
