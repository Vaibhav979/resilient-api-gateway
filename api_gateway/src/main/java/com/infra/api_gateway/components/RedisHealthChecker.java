package com.infra.api_gateway.components;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthChecker {

    private final StringRedisTemplate redis;
    private volatile boolean lastKnownUp = false;

    public RedisHealthChecker(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean isRedisUp() {
        try {
            redis.opsForValue().set("__health", "ok", Duration.ofSeconds(2));
            String v = redis.opsForValue().get("__health");
            boolean up = "ok".equals(v);
            lastKnownUp = up || lastKnownUp; // degrade instead of die
            return lastKnownUp;
        } catch (Exception e) {
            return lastKnownUp;
        }
    }
}
