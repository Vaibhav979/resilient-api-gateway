package com.infra.api_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infra.api_gateway.components.RedisHealthChecker;
import com.infra.api_gateway.exception.RateLimitExceededException;
import com.infra.api_gateway.service.DataService;
import com.infra.api_gateway.service.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class ApiController {

    public final DataService dataService;
    // private final RedisHealthChecker redisHealthChecker;
    private final RateLimiterService rateLimiter;

    public ApiController(DataService dataService, RedisHealthChecker redisHealthChecker,
            RateLimiterService rateLimiter) {
        this.dataService = dataService;
        // this.redisHealthChecker = redisHealthChecker;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/data")
    public Map<String, Object> getData(@RequestParam(required = false) String mode, HttpServletRequest request) {
        // if (!redisHealthChecker.isRedisUp()) { removing hard dependency on redis
        // throw new IllegalStateException("Redis is down");
        // }

        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.isAllowed(clientIp)) {
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }
        return dataService.getData(mode);
    }
}
