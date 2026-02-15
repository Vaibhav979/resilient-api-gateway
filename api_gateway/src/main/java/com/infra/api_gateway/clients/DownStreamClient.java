package com.infra.api_gateway.clients;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infra.api_gateway.entities.CachedResponse;
import com.infra.api_gateway.repositories.CachedResponseRepository;
import static com.infra.api_gateway.utils.StructuredLogger.kv;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

@Service
public class DownStreamClient {

    private static final Logger log = LoggerFactory.getLogger(DownStreamClient.class);

    private final RestTemplate restTemplate;

    @Value("${DOWNSTREAM_URL:http://localhost:8081/downstream/data}")
    private String downstreamUrl;

    // Circuit Breakers
    private final CircuitBreaker downstreamBreaker;
    private final CircuitBreaker redisBreaker;
    private final CircuitBreaker databaseBreaker;

    private final Retry retry;

    private final RedisTemplate<String, Object> redisTemplate;
    private final CachedResponseRepository cachedResponseRepository;

    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    // Maximum age for database cache - only return fresh data from DB
    private static final Duration MAX_DB_CACHE_AGE = Duration.ofSeconds(30);

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("DownStreamClient initialized with downstreamUrl: {}", downstreamUrl);
    }

    public DownStreamClient(
            RestTemplate restTemplate,
            CircuitBreakerRegistry breakerRegistry,
            RetryRegistry retryRegistry,
            RedisTemplate<String, Object> redisTemplate,
            CachedResponseRepository cachedResponseRepository) {

        this.restTemplate = restTemplate;

        // Circuit breakers
        this.downstreamBreaker = breakerRegistry.circuitBreaker("downstreamService");
        this.redisBreaker = breakerRegistry.circuitBreaker("redis");
        this.databaseBreaker = breakerRegistry.circuitBreaker("database");

        this.retry = retryRegistry.retry("downstreamService");

        this.redisTemplate = redisTemplate;
        this.cachedResponseRepository = cachedResponseRepository;

        this.objectMapper = new ObjectMapper();
    }

    // ==========================
    // MAIN ENTRY
    // ==========================

    public Map<String, Object> getData(Long delay, Boolean fail) {

        String cacheKey = buildCacheKey(delay, fail);

        // -------------------------
        // 1. Try Redis (Fail-Fast with timeout)
        // -------------------------

        Map<String, Object> redisData = safeRedisGet(cacheKey);

        if (redisData != null) {
            log.debug("Cache hit for key={}", cacheKey,
                    kv("cacheKey", cacheKey));
            return redisData;
        }

        log.debug("Cache miss for key={}", cacheKey,
                kv("cacheKey", cacheKey));

        // -------------------------
        // 2. Try Database (with circuit breaker and age check)
        // -------------------------

        Optional<CachedResponse> dbData = safeDatabaseGet(cacheKey);

        if (dbData.isPresent()) {
            CachedResponse cached = dbData.get();

            // Check if cache is still fresh - don't return stale data
            Instant cacheTime = cached.getUpdatedAt();
            Instant now = Instant.now();
            long cacheAgeSeconds = Duration.between(cacheTime, now).getSeconds();

            if (cacheAgeSeconds > MAX_DB_CACHE_AGE.getSeconds()) {
                log.info("Database cache too old ({}s), ignoring and calling downstream", cacheAgeSeconds);
                // Don't return stale data - proceed to call downstream
            } else {
                log.debug("Returning fresh database cache (age: {}s)", cacheAgeSeconds);
                Map<String, Object> result = deserialize(cached.getJsonData());

                // Refill Redis (best effort)
                safeRedisSet(cacheKey, result);

                return result;
            }
        }

        // -------------------------
        // 3. Call Downstream
        // -------------------------

        Map<String, Object> result = callDownstreamWithResilience(delay, fail);

        // -------------------------
        // 4. Persist Cache
        // -------------------------

        saveToDatabase(cacheKey, result);

        safeRedisSet(cacheKey, result);

        return result;
    }

    // ==========================
    // REDIS (SAFE with timeout)
    // ==========================

    private Map<String, Object> safeRedisGet(String key) {

        try {
            // Use the circuit breaker with a timeout wrapper
            Supplier<Object> supplier = CircuitBreaker.decorateSupplier(
                    redisBreaker,
                    () -> {
                        Object value = redisTemplate.opsForValue().get(key);
                        return value;
                    });

            // Execute with timeout using Spring's RedisTemplate configuration
            // The Redis connection/request timeout is configured in application.yml
            return (Map<String, Object>) supplier.get();

        } catch (Exception e) {
            // Fail silently → fallback to database
            return null;
        }
    }

    private void safeRedisSet(String key, Map<String, Object> value) {

        try {
            Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
                    redisBreaker,
                    () -> {
                        redisTemplate.opsForValue()
                                .set(key, value, CACHE_TTL);
                        return true;
                    });

            supplier.get();
        } catch (Exception ignored) {
            // ignore
        }
    }

    // ==========================
    // DATABASE (SAFE with circuit breaker)
    // ==========================

    private Optional<CachedResponse> safeDatabaseGet(String key) {

        try {
            Supplier<Optional<CachedResponse>> supplier = CircuitBreaker.decorateSupplier(
                    databaseBreaker,
                    () -> cachedResponseRepository.findById(key));

            return supplier.get();

        } catch (Exception e) {
            // Fail silently → call downstream
            return Optional.empty();
        }
    }

    private void safeDatabaseSave(String key, Map<String, Object> result) {

        try {
            CachedResponse entity = new CachedResponse();
            entity.setCacheKey(key);
            entity.setJsonData(serialize(result));
            entity.setUpdatedAt(Instant.now());

            Supplier<CachedResponse> supplier = CircuitBreaker.decorateSupplier(
                    databaseBreaker,
                    () -> cachedResponseRepository.save(entity));

            supplier.get();

        } catch (Exception ignored) {
            // ignore
        }
    }

    // ==========================
    // DOWNSTREAM (RESILIENT)
    // ==========================

    private Map<String, Object> callDownstreamWithResilience(
            Long delay,
            Boolean fail) {

        Supplier<Map<String, Object>> supplier = () -> callDownstreamService(delay, fail);

        // Add circuit breaker
        supplier = CircuitBreaker.decorateSupplier(
                downstreamBreaker,
                supplier);

        // Add retry
        supplier = Retry.decorateSupplier(retry, supplier);

        try {
            return supplier.get();

        } catch (Exception e) {
            log.error("Downstream service call failed: {}", e.getMessage(), e);

            // Ultimate fallback
            return Map.of(
                    "status", "degraded",
                    "source", "fallback",
                    "message", "Downstream unavailable",
                    "timestamp", Instant.now().toString());
        }
    }

    // ==========================
    // HTTP CALL
    // ==========================

    private Map<String, Object> callDownstreamService(
            Long delay,
            Boolean fail) {

        String baseUrl = downstreamUrl;

        log.info("Calling downstream service with URL: {}", baseUrl);

        StringBuilder url = new StringBuilder(baseUrl);

        if (delay != null || fail != null) {
            url.append("?");
        }

        if (delay != null) {
            url.append("delayMs=").append(delay);
        }

        if (fail != null) {

            if (delay != null) {
                url.append("&");
            }

            url.append("fail=").append(fail);
        }

        return restTemplate.getForObject(
                url.toString(),
                Map.class);
    }

    // ==========================
    // DATABASE
    // ==========================

    private void saveToDatabase(String key,
            Map<String, Object> result) {

        safeDatabaseSave(key, result);
    }

    // ==========================
    // CACHE KEY
    // ==========================

    private String buildCacheKey(Long delay,
            Boolean fail) {

        StringBuilder sb = new StringBuilder("downstreamData:");

        if (delay != null) {
            sb.append("delay=").append(delay).append(":");
        }

        if (fail != null) {
            sb.append("fail=").append(fail).append(":");
        }

        return sb.toString();
    }

    // ==========================
    // SERIALIZATION
    // ==========================

    private String serialize(Map<String, Object> data) {

        try {
            return objectMapper.writeValueAsString(data);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Serialization failed", e);
        }
    }

    private Map<String, Object> deserialize(String json) {

        try {
            return objectMapper.readValue(json, Map.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Deserialization failed", e);
        }
    }
}
