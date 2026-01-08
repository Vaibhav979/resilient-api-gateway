package com.infra.api_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 4xx: Client Errors
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Map<String, Object>> handleClientException(ClientException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Bad Request",
                "timestamp", Instant.now(),
                "message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 5xx: Server Errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleServerException(Exception ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Internal Server Error",
                "message", "Unexpected failure occurred",
                "timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}