package com.infra.api_gateway.components;

import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class CircuitBreakerMetrics {

    public CircuitBreakerMetrics(
            CircuitBreakerRegistry registry,
            MeterRegistry meterRegistry) {

        CircuitBreaker cb = registry.circuitBreaker("downstreamService");

        Gauge.builder("gateway_circuitbreaker_state", cb,
                breaker -> breaker.getState().getOrder())
                .description("0=CLOSED,1=OPEN,2=HALF_OPEN")
                .register(meterRegistry);
    }
}
