package com.infra.api_gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;

import java.time.Duration;
import static com.infra.api_gateway.utils.StructuredLogger.kv;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private static final int MAX_REQUESTS = 50;
    private static final Duration WINDOW = Duration.ofSeconds(10);
    private final Counter allowedRequests;
    private final Counter blockedRequests;
    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate, Counter allowedRequests, Counter blockedRequests) {
        this.redisTemplate = redisTemplate;
        this.allowedRequests = allowedRequests;
        this.blockedRequests = blockedRequests;
    }

    public boolean isAllowed(String clientId) {

        String key = "rate_limit:api-gateway:" + clientId;

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                // slightly extended expiry avoids race edge cases
                redisTemplate.expire(key, WINDOW.plusSeconds(1));
            }

            if (count != null && count > MAX_REQUESTS) {
                blockedRequests.increment();
            } else {
                allowedRequests.increment();
            }

            boolean allowed = count != null && count <= MAX_REQUESTS;

            if (!allowed) {
                log.warn("Rate limit exceeded",
                        kv("clientId", clientId),
                        kv("requestCount", count),
                        kv("maxRequests", MAX_REQUESTS),
                        kv("windowSeconds", WINDOW.getSeconds()));
            }

            return allowed;

        } catch (Exception e) {
            log.error("Redis unavailable — rate limiter fail-open", e);
            return true; // fail-open
        }
    }
}
