package com.caco.sitedocaco.modules.events.dto.request;

import com.caco.sitedocaco.modules.events.entity.EventGalleryItem;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CreateGalleryItemDTO(
        MultipartFile image,
        String mediaUrl,
        @NotNull EventGalleryItem.MediaType type,
        String caption
) {
    public void validate() {
        // Validar que apenas um dos dois campos está preenchido
        if ((image != null && mediaUrl != null) || (image == null && mediaUrl == null)) {
            throw new IllegalArgumentException("Deve fornecer apenas 'image' (multipart) OU 'mediaUrl', nunca ambos ou nenhum");
        }
    }
}

