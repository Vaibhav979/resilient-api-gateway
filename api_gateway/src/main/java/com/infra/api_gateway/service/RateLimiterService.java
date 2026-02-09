package com.infra.api_gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import static com.infra.api_gateway.utils.StructuredLogger.kv;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofSeconds(10);

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientId) {
        String key = "rate_limit:" + clientId;
        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, WINDOW);
            }

            if (count != null && count > MAX_REQUESTS) {
                log.warn("Rate limit exceeded for ip={}", clientId,
                        kv("clientId", clientId),
                        kv("requestCount", count),
                        kv("maxRequests", MAX_REQUESTS),
                        kv("windowSeconds", WINDOW.getSeconds()));
            }

            return count != null && count <= MAX_REQUESTS;
        } catch (Exception e) {
            // In case of Redis failure, allow the request (fail-open)
            return true;
        }
    }
}
