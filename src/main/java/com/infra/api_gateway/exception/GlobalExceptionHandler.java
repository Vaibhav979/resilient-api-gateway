package com.infra.api_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 503 — Infra down
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                Map.of(
                        "error", "Service Unavailable",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()));
    }

    // 4xx — Client mistakes
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Map<String, Object>> handleClientException(ClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "Bad Request",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()));
    }

    // 5xx — Everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleServerException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "error", "Internal Server Error",
                        "message", "Unexpected failure occurred",
                        "timestamp", Instant.now().toString()));
    }

    // 429 Too many requests exception
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(429).body(
                Map.of(
                        "error", "Too Many Requests",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()));
    }
}