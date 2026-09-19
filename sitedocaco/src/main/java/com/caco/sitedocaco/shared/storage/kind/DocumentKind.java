package com.caco.sitedocaco.shared.storage.kind;

import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.storage.UploadRequest;

public enum DocumentKind implements FileKind {

    EXAM_PDF("exam-pdf", 32L * 1024 * 1024);

    private final String code;
    private final long maxSizeBytes;

    DocumentKind(String code, long maxSizeBytes) {
        this.code = code;
        this.maxSizeBytes = maxSizeBytes;
    }

    @Override
    public String code() { return code; }

    @Override
    public Category category() { return Category.DOCUMENT; }

    @Override
    public void validate(UploadRequest request) {
        if (request.sizeBytes() > maxSizeBytes) {
            throw new BusinessRuleException(String.format(
                    "Documento '%s' excede o tamanho máximo de %dMB.",
                    code, maxSizeBytes / (1024 * 1024)));
        }

        boolean isPdfMime = "application/pdf".equals(request.contentType());
        boolean isPdfExt  = request.originalFilename() != null
                && request.originalFilename().toLowerCase().endsWith(".pdf");

        if (!isPdfMime && !isPdfExt) {
            throw new BusinessRuleException(
                    "Documento '" + code + "' deve ser um PDF.");
        }
    }
}