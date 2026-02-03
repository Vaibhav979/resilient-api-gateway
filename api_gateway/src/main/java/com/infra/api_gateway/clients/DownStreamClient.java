package com.infra.api_gateway.clients;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;

@Service
public class DownStreamClient {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    public DownStreamClient(RestTemplate restTemplate, CircuitBreakerRegistry registry, RetryRegistry retryRegistry, RedisTemplate<String, Object> redisTemplate) {
        this.circuitBreaker = registry.circuitBreaker("downstreamService");
        this.restTemplate = restTemplate;
        this.retry = retryRegistry.retry("downstreamService");
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> getData(Long delay, Boolean fail) {

        String cacheKey = buildCacheKey(delay, fail);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (Map<String, Object>) cached;
        }

        Supplier<Map<String, Object>> supplier = () -> callDownstreamService(delay, fail);

        Supplier<Map<String, Object>> decoratedSupplier = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, supplier));
        Map<String, Object> result = decoratedSupplier.get();

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);

        return result;
    }

    private String buildCacheKey(Long delay, Boolean fail) {
        StringBuilder sb = new StringBuilder("downstreamData:");
        if (delay != null) {
            sb.append("delay=").append(delay).append(":");
        }
        if (fail != null) {
            sb.append("fail=").append(fail).append(":");
        }
        return sb.toString();
    }

    private Map<String, Object> callDownstreamService(Long delay, Boolean fail) {
        String url = "http://downstream-service:8080/downstream/data";

        StringBuilder sb = new StringBuilder(url);

        if (delay != null || fail != null) {
            sb.append("?");
        }

        if (delay != null) {
            sb.append("delayMs=").append(delay);
        }

        if (fail != null) {
            if (delay != null) {
                sb.append("&");
            }
            sb.append("fail=").append(fail);
        }
        url = sb.toString();

        return restTemplate.getForObject(url, Map.class);
    }
}
