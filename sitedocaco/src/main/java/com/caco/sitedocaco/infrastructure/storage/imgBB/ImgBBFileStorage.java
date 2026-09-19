package com.caco.sitedocaco.infrastructure.storage.imgBB;

import com.caco.sitedocaco.shared.storage.FileStorage;
import com.caco.sitedocaco.shared.storage.StorageException;
import com.caco.sitedocaco.shared.storage.StoredFile;
import com.caco.sitedocaco.shared.storage.UploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImgBBFileStorage implements FileStorage {
    private ImgBBClient imgBBClient;

    @Override
    public StoredFile store(UploadRequest request) throws StorageException {
        request.kind().validate(request);
        String url = imgBBClient.upload(request.content(), request.originalFilename());

        return new StoredFile(url, request.kind(), request.sizeBytes());
    }

    @Override
    public void delete(String url) throws StorageException {
        // ImgBB não expõe API pública de delete confiável.
        // A limpeza física ficará a cargo do próximo provedor (S3, Cloudinary, etc.).
        // Não lançamos exceção: delete é sempre best-effort.
        log.debug("Delete ignorado para ImgBB (provedor sem API de remoção): {}", url);
    }
}
