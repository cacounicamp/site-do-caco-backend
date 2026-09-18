package com.caco.sitedocaco.features.stickers.dto.response;

import java.util.List;

public record RedemptionCodeBatchResponseDTO(
        int quantity,
        List<String> codes
) {
}

