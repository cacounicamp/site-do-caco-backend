package com.caco.sitedocaco.modules.whatsapp.dto.response;

import com.caco.sitedocaco.modules.whatsapp.entity.WhatsAppGroup;
import com.caco.sitedocaco.shared.entity.CourseType;

import java.util.UUID;

public record WhatsAppGroupDTO(
        UUID id,
        CourseType course,
        int entryYear,
        String whatsappLink
) {
    public static WhatsAppGroupDTO fromEntity(WhatsAppGroup group) {
        return new WhatsAppGroupDTO(
                group.getId(),
                group.getCourse(),
                group.getEntryYear(),
                group.getWhatsappLink()
        );
    }
}

