package com.infra.api_gateway.exception;

public class BadRequestException extends ClientException {

    public BadRequestException(String message) {
        super(message);
    }
}
