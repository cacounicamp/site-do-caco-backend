package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormOption;

public record OptionDTO(String code, String label, boolean allowsFreeText) {

    public static OptionDTO fromEntity(FormOption option) {
        return new OptionDTO(option.getCode(), option.getLabel(), option.isAllowsFreeText());
    }
}
