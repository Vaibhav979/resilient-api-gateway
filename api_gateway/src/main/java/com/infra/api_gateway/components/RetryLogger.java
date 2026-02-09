package com.infra.api_gateway.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import static com.infra.api_gateway.utils.StructuredLogger.kv;

@Component
public class RetryLogger {

    private static final Logger log = LoggerFactory.getLogger(RetryLogger.class);

    public RetryLogger(RetryRegistry registry) {
        Retry retry = registry.retry("downstreamRetry");

        retry.getEventPublisher()
                .onRetry(event -> {
                    log.warn("Downstream failed",
                            kv("retryAttempt", event.getNumberOfRetryAttempts()),
                            kv("waitIntervalMs", event.getWaitInterval().toMillis()),
                            kv("exception", event.getLastThrowable().getMessage()));
                });

        retry.getEventPublisher()
                .onError(event -> {
                    log.error("Downstream failed",
                            kv("totalAttempts", event.getNumberOfRetryAttempts()),
                            kv("exception", event.getLastThrowable().getMessage()),
                            event.getLastThrowable());
                });
    }
}
