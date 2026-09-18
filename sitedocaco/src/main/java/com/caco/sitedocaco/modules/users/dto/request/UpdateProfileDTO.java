package com.caco.sitedocaco.modules.users.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileDTO(
        String name,
        MultipartFile avatar
) {
    public UpdateProfileDTO {
        // Este construtor vazio permite que Spring crie o objeto mesmo com avatar null
    }
}