package com.infra.api_gateway.components;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class RedisWarmup {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void warmup() {
        try {
            redisTemplate.opsForValue().set("health", "ok");
            System.out.println("Redis warmup successful");
        } catch (Exception e) {
            System.out.println("Redis not ready, skipping warmup");
        }
    }
}
