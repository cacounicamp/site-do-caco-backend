package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormOption;

import java.util.UUID;

public record OptionAdminDTO(
        UUID id,
        String code,
        String label,
        boolean allowsFreeText,
        boolean active,
        int displayOrder
) {
    public static OptionAdminDTO fromEntity(FormOption option) {
        return new OptionAdminDTO(
                option.getId(),
                option.getCode(),
                option.getLabel(),
                option.isAllowsFreeText(),
                option.isActive(),
                option.getDisplayOrder()
        );
    }
}
