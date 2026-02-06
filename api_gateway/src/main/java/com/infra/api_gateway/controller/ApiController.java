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

    public final DataService dataService;
    private final RateLimiterService rateLimiter;
    private final DownStreamClient downstreamClient;
    private final Counter requestCounter;

    public ApiController(DataService dataService, RedisHealthChecker redisHealthChecker,
            RateLimiterService rateLimiter, DownStreamClient downstreamClient, Counter requestCounter) {
        this.dataService = dataService;
        this.rateLimiter = rateLimiter;
        this.downstreamClient = downstreamClient;
        this.requestCounter = requestCounter;
    }

    @GetMapping("/data")
    public Map<String, Object> getData(HttpServletRequest request,
            @RequestParam(required = false) Long delayMs,
            @RequestParam(required = false) Boolean fail) {
        requestCounter.increment();

        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.isAllowed(clientIp)) {
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }

        return downstreamClient.getData(delayMs, fail);
    }
}
