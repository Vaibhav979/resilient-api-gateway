package com.infra.api_gateway.clients;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;

@Service
public class DownStreamClient {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public DownStreamClient(RestTemplate restTemplate, CircuitBreakerRegistry registry, RetryRegistry retryRegistry) {
        this.circuitBreaker = registry.circuitBreaker("downstreamService");
        this.restTemplate = restTemplate;
        this.retry = retryRegistry.retry("downstreamService");
    }

    public Map<String, Object> getData(Long delay, Boolean fail) {
        Supplier<Map<String, Object>> supplier = () -> callDownstreamService(delay, fail);

        Supplier<Map<String, Object>> decoratedSupplier = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, supplier));
        return decoratedSupplier.get();
    }

    public Map<String, Object> callDownstreamService(Long delay, Boolean fail) {
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
