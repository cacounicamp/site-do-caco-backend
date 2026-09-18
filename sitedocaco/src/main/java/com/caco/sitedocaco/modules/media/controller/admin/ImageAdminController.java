package com.caco.sitedocaco.modules.media.controller.admin;

import com.caco.sitedocaco.modules.media.dto.response.ImageUploadResponseDTO;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/images")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
// Upload de imagem: consome bandwidth real do ImgBB — limite bastante conservador
@RateLimit(capacity = 10, refillTokens = 10)
public class ImageAdminController {

    private final ImgBBService imgBBService;

    /**
     * Upload de imagem genérica
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @RequestParam("image") @NotNull MultipartFile image) throws IOException {
        String imageUrl = imgBBService.uploadImage(image);

        return ResponseEntity.ok(new ImageUploadResponseDTO(imageUrl));
    }
}