package com.caco.sitedocaco.shared.storage.kind;

import com.caco.sitedocaco.shared.storage.UploadRequest;

public sealed interface FileKind permits ImageKind, DocumentKind {

    String code();

    Category category();

    void validate(UploadRequest request);

    enum Category {
        IMAGE,
        DOCUMENT,
        VIDEO,
        AUDIO,
        OTHER
    }
}
