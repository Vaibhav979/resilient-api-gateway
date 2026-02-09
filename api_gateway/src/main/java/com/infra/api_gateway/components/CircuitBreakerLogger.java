package com.infra.api_gateway.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import static com.infra.api_gateway.utils.StructuredLogger.kv;

@Component
public class CircuitBreakerLogger {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerLogger.class);

    public CircuitBreakerLogger(CircuitBreakerRegistry registry) {
        CircuitBreaker cb = registry.circuitBreaker("downstreamService");

        cb.getEventPublisher()
                .onStateTransition(
                        event -> {
                            log.info("Circuit breaker '{}' changed state from {} to {}",
                                    kv("circuitBreaker", cb.getName()),
                                    kv("fromState", event.getStateTransition().getFromState()),
                                    kv("toState", event.getStateTransition().getToState()));

                            if (event.getStateTransition()
                                    .getToState() == io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN) {
                                log.info("Circuit breaker opened for downstream",
                                        kv("circuitBreaker", cb.getName()));
                            }
                        });
    }

}
