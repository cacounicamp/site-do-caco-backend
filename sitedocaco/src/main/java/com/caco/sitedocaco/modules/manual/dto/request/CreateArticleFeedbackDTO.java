package com.caco.sitedocaco.modules.manual.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateArticleFeedbackDTO(
        @NotNull
        Boolean isHelpful,

        String comment
) {}