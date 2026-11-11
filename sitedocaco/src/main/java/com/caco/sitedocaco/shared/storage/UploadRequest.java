package com.caco.sitedocaco.shared.storage;

import com.caco.sitedocaco.shared.storage.kind.FileKind;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record UploadRequest(
        FileKind kind,
        byte[] content,
        String originalFilename,
        String contentType
) {
    public long sizeBytes() {
        return content.length;
    }

    public static UploadRequest of(MultipartFile file, FileKind kind) throws IOException {
        return new UploadRequest(
                kind,
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType()
        );
    }
}