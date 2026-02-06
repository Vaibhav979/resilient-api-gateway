package com.infra.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter requestCounter(MeterRegistry registry) {
        return Counter.builder("gateway_requests_total")
                .description("Total API Gateway requests")
                .baseUnit("requests")
                .register(registry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("gateway_errors_total")
                .description("Total API Gateway errors")
                .baseUnit("errors")
                .register(registry);
    }
}
