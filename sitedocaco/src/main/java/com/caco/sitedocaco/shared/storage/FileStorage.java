package com.caco.sitedocaco.shared.storage;

public interface FileStorage {
    StoredFile store(UploadRequest request) throws StorageException;
    void delete(String url) throws StorageException;
}
