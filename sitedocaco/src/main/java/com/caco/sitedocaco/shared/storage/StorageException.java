package com.caco.sitedocaco.shared.storage;

import com.caco.sitedocaco.shared.exception.InfrastructureException;

public class StorageException extends InfrastructureException {
    public StorageException(String message, Throwable cause) { super(message, cause); }
    public StorageException(String message) { super(message); }
}
