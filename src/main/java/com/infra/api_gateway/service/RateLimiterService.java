package com.infra.api_gateway.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {
    
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofSeconds(10);

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientId) {
        String key = "rate_limit:" + clientId;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW);
        }
        return count != null && count <= MAX_REQUESTS;
    }
}
