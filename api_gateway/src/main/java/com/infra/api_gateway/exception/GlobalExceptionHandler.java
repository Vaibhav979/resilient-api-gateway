package com.infra.api_gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.micrometer.core.instrument.Counter;
// import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Map;
import static com.infra.api_gateway.utils.StructuredLogger.kv;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Counter errorCounter;
    private final Counter throttledCounter;

    public GlobalExceptionHandler(Counter errorCounter, Counter throttledCounter) {
        this.errorCounter = errorCounter;
        this.throttledCounter = throttledCounter;
    }

    // 503 — Infra down
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        errorCounter.increment();
        log.warn("Serving fallback response",
                kv("exceptionType", "IllegalStateException"),
                kv("message", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                Map.of(
                        "error", "Service Unavailable",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()));
    }

    // 4xx — Client mistakes
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Map<String, Object>> handleClientException(ClientException ex) {
        errorCounter.increment();
        log.warn("Serving fallback response",
                kv("exceptionType", "ClientException"),
                kv("message", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "Bad Request",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()));
    }

    // 5xx — Everything else
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<Map<String, Object>> handleServerException(Exception
    // ex, HttpServletRequest request) throws Exception {
    // if (request.getRequestURI().startsWith("/actuator")) {
    // throw ex;
    // }
    // errorCounter.increment();
    // log.warn("Serving fallback response",
    // kv("exceptionType", "Exception"),
    // kv("message", ex.getMessage()));
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
    // Map.of(
    // "error", "Internal Server Error",
    // "message", "Unexpected failure occurred",
    // "timestamp", Instant.now().toString()));
    // }

    // 429 Too many requests exception
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitExceededException ex) {
        throttledCounter.increment();
        log.warn("Serving fallback response",
                kv("exceptionType", "RateLimitExceededException"),
                kv("message", ex.getMessage()));
        return ResponseEntity.status(429).body(
                Map.of(
                        "error", "Too Many Requests",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()));
    }
}
