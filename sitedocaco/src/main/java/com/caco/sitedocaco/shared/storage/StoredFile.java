package com.caco.sitedocaco.shared.storage;

import com.caco.sitedocaco.shared.storage.kind.FileKind;

public record StoredFile(
        String url,
        FileKind kind,
        long sizeBytes
) {}