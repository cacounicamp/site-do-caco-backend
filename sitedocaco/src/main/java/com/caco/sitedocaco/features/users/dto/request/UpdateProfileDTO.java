package com.caco.sitedocaco.features.users.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileDTO(
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
        String name,
        MultipartFile avatar
) {
    public UpdateProfileDTO {
        // Este construtor vazio permite que Spring crie o objeto mesmo com avatar null
    }
}