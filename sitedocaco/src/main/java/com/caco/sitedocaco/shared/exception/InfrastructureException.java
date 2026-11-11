package com.caco.sitedocaco.shared.exception;

public abstract class InfrastructureException extends RuntimeException {
    protected InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
    protected InfrastructureException(String message){
        super(message);
    }
}
