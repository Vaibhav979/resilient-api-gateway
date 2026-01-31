package com.infra.api_gateway.components;

import org.springframework.stereotype.Component;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

@Component
public class RetryLogger {

    public RetryLogger(RetryRegistry registry) {
        Retry retry = registry.retry("downstreamRetry");

        retry.getEventPublisher()
                .onRetry(event -> System.out.println(
                        "Retry attempt: " +
                                event.getNumberOfRetryAttempts()));
    }
}
