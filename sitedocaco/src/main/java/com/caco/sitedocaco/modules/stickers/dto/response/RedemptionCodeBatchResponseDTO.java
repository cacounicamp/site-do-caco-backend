package com.caco.sitedocaco.modules.stickers.dto.response;

import java.util.List;

public record RedemptionCodeBatchResponseDTO(
        int quantity,
        List<String> codes
) {
}

