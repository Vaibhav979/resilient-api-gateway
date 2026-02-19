package com.infra.api_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infra.api_gateway.components.RedisHealthChecker;
import com.infra.api_gateway.exception.RateLimitExceededException;
import com.infra.api_gateway.service.DataService;
import com.infra.api_gateway.service.RateLimiterService;

import io.micrometer.core.instrument.Counter;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;

// import static org.junit.jupiter.api.Assertions.fail;
import com.infra.api_gateway.clients.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DataService dataService;
    private final RateLimiterService rateLimiter;
    private final DownStreamClient downstreamClient;

    private final Counter requestCounter;
    private final Counter throttledCounter;
    private final Counter allowedCounter;
    private final Counter errorCounter;

    public ApiController(
            DataService dataService,
            RateLimiterService rateLimiter,
            DownStreamClient downstreamClient,
            Counter requestCounter,
            Counter throttledCounter,
            Counter allowedCounter,
            Counter errorCounter) {

        this.dataService = dataService;
        this.rateLimiter = rateLimiter;
        this.downstreamClient = downstreamClient;
        this.requestCounter = requestCounter;
        this.throttledCounter = throttledCounter;
        this.allowedCounter = allowedCounter;
        this.errorCounter = errorCounter;
    }

    @GetMapping("/data")
    public Map<String, Object> getData(
            HttpServletRequest request,
            @RequestParam(required = false) Long delayMs,
            @RequestParam(required = false) Boolean fail) {

        requestCounter.increment();

        String clientIp = extractClientIp(request);

        if (!rateLimiter.isAllowed(clientIp)) {
            throttledCounter.increment();
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Please try again later.");
        }

        try {
            Map<String, Object> response =
                    downstreamClient.getData(delayMs, fail);

            allowedCounter.increment();
            return response;

        } catch (Exception ex) {
            errorCounter.increment();
            throw ex;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            String firstIp = forwarded.split(",")[0].trim();
            if (!firstIp.isEmpty()) {
                return firstIp;
            }
        }

        return request.getRemoteAddr();
    }
}
