package com.infra.api_gateway.exception;

public abstract class ClientException extends RuntimeException {

    protected ClientException(String message) {
        super(message);
    }
}
