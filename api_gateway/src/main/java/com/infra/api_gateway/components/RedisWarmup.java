package com.infra.api_gateway.components;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisWarmup {

    private final StringRedisTemplate redis;

    public RedisWarmup(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @PostConstruct
    public void warmup() {
        // Force real TCP + Redis protocol handshake at startup
        redis.opsForValue().set("__warmup", "ok");
    }
}
