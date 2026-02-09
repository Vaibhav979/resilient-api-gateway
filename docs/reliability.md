# Reliability & SLO Policy

This document defines the reliability targets and alerting strategy for the API Gateway.

These values are based on load testing, failure injection, and observed system behavior.

---

## Service Level Objectives (SLOs)

- Availability: 99.5% successful requests per month
- Latency:
  - p95 < 500ms
  - p99 < 2s
- Throttling: < 5% of total requests

---

## Error Budget

Given the availability SLO, the system operates with an error budget of:

- 0.5% failed requests per month

This budget allows controlled experimentation while maintaining user experience.

---

## Alerting Policy

Alerts are triggered only when user experience or system stability is at risk.

### Critical Alerts

- Error rate > 2% for 5 minutes
- Circuit breaker open > 2 minutes
- p99 latency > 3 seconds
- Throttled requests > 20%

---

## Non-Alerting Signals

The following signals are monitored via dashboards but do not trigger alerts:

- Single or transient errors
- CPU usage < 85%
- Short-lived cache misses
- Brief circuit breaker openings

---

## Philosophy

Alerts are designed to be actionable and aligned with user impact and error budget burn rate.
