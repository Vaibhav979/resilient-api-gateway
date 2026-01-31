package com.infra.api_gateway.clients;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Service
public class DownStreamClient {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;

    public DownStreamClient(RestTemplate restTemplate, CircuitBreakerRegistry registry) {
        this.circuitBreaker = registry.circuitBreaker("downstreamService");
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getData(Long delay, Boolean fail) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, () -> callDownstreamService(delay, fail)).get();
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
