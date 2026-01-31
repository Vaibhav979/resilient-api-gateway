package com.infra.api_gateway.components;

import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class CircuitBreakerLogger {

    public CircuitBreakerLogger(CircuitBreakerRegistry registry) {
        CircuitBreaker cb = registry.circuitBreaker("downstreamService");

        cb.getEventPublisher()
                .onStateTransition(
                        event -> System.out.println("CircuitBreaker '" + cb.getName() + "' changed state from "
                                + event.getStateTransition().getFromState() + " to "
                                + event.getStateTransition().getToState()));
    }

}
