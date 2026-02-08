### Latency Metrics

We track:

- p50: median latency
- p95: slow-user latency
- p99: worst-case latency

Tail latency is prioritized over averages to detect degraded user experience early.

### Circuit Breaker Metrics

We monitor:

- resilience4j_circuitbreaker_state
- resilience4j_circuitbreaker_calls
- resilience4j_circuitbreaker_failure_rate

Alerts are triggered when breakers remain open or block a significant portion of traffic.

### Rate Limiting Metrics

- gateway_ratelimit_hits
- gateway_throttled_requests

Used to detect abuse, misconfigured clients,
and traffic spikes.
