package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.OptionSet;

import java.util.List;
import java.util.UUID;

public record OptionSetDTO(
        UUID id,
        String slug,
        String name,
        List<OptionAdminDTO> options
) {
    public static OptionSetDTO from(OptionSet set, List<FormOption> options) {
        return new OptionSetDTO(
                set.getId(),
                set.getSlug(),
                set.getName(),
                options.stream().map(OptionAdminDTO::fromEntity).toList()
        );
    }
}
