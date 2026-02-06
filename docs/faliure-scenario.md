# Failure Scenarios

This document describes expected system behavior under common failure
conditions, including cache outages, downstream instability, and latency
spikes.

The goal is to ensure predictable degradation and avoid cascading failures.

## System Architecture (Normal State)

Client
|
v
API Gateway
|
+--> Redis (Cache)
|
+--> PostgreSQL (Fallback)
|
v
Downstream Service

## Scenario: Client sends invalid request (wrong endpoint or method)

**Expected Behavior:**

- Server returns `404 Not Found` or `405 Method Not Allowed`
- Request does not reach business logic
- No retries are performed
- No alerts are triggered

**Rationale:**
These errors indicate incorrect client behavior and do not represent system unreliability.

<!-- ## Scenario: Redis Unavailable

**Trigger**

- Redis container stopped or unreachable

**Expected Behavior**

- API fails fast with 503 Service Unavailable

**User Impact**

- Requests rejected quickly
- No hanging connections

**Reasoning**

- Prevent thread exhaustion
- Make dependency failure explicit -->

## Scenario: Redis Unavailable

Trigger:

- Redis process is stopped or unreachable

Impact:

- Cache layer becomes unavailable
- Rate limiting storage may be affected

System Behavior:

- Cache lookups fail
- System falls back to PostgreSQL
- Rate limiting fails open
- Requests continue to be served

User Impact:

- Slight increase in latency
- No functional outage

Recovery:

- Redis restarts
- Cache is gradually repopulated

Reasoning:

- Redis is treated as a performance optimization
- Availability is prioritized over strict enforcement

## Scenario: Redis High Latency

Trigger:

- Redis response time exceeds configured timeout

Impact:

- Cache operations block temporarily

System Behavior:

- Requests fail fast after timeout
- Cache is bypassed
- Database fallback is used

User Impact:

- Increased tail latency
- No complete outage

Recovery:

- Redis latency normalizes
- Cache resumes automatically

Reasoning:

- Timeouts convert slowness into bounded failure
- Prevents thread exhaustion

## Scenario: Downstream Service Failure

Trigger:

- Downstream returns HTTP 5xx
- Service crashes

Impact:

- Primary data source unavailable

System Behavior:

- Retry attempts executed
- Circuit breaker opens after threshold
- Cached or DB data served if available
- New downstream calls blocked

User Impact:

- Possible stale data
- Reduced functionality

Recovery:

- Circuit transitions to HALF-OPEN
- Successful probes restore CLOSED state

Reasoning:

- Prevents repeated calls to unhealthy service
- Protects gateway resources

## Scenario: Downstream High Latency

Trigger:

- Downstream response time exceeds client timeout

Impact:

- Requests risk blocking threads

System Behavior:

- Request times out
- Retry with backoff applied
- Circuit breaker may open
- Cache or DB used if available

User Impact:

- Occasional delays
- Some failed requests

Recovery:

- Latency returns to normal
- Circuit closes

Reasoning:

- Fail-fast prevents cascading failures
- Backoff reduces pressure on dependency

## Scenario: PostgreSQL Unavailable

Trigger:

- Database process stops
- Network partition

Impact:

- Durable cache unavailable

System Behavior:

- Redis used if available
- Downstream accessed directly
- No persistent fallback

User Impact:

- Increased load on downstream
- Potential latency increase

Recovery:

- Database restarts
- Persistence resumes

Reasoning:

- Database is secondary cache
- System can operate temporarily without it

## Scenario: Complete Cache Failure (Redis + DB)

Trigger:

- Redis and PostgreSQL unavailable

Impact:

- No cached data available

System Behavior:

- All requests routed to downstream
- Circuit breaker monitors health
- Rate limiting still applied

User Impact:

- Higher latency
- Increased failure risk

Recovery:

- Cache layers restored
- Data repopulated

Reasoning:

- Downstream is source of truth
- System degrades to minimum viable state

## Scenario: Rate Limit Exceeded

Trigger

- Client exceeds request quota within time window

Expected Behavior

- API returns 429 Too Many Requests

User Impact

- Client must retry later
- Other users unaffected

Reasoning

- Protect system from overload

## Scenario: Redis Slow or Unavailable

Trigger

- Redis latency exceeds timeout or connection fails

Expected Behavior

- Rate limiting bypassed
- Requests continue to be served

User Impact

- No immediate outage
- Potential performance degradation under heavy load

Reasoning

- Preserve availability over strict enforcement

## Scenario: Downstream Repeated Failures

Trigger

- Failure rate exceeds threshold

Expected Behavior

- Circuit breaker opens
- Downstream calls blocked

User Impact

- Fast failures
- No cascading latency

Reasoning

- Protect threads and queues

## Scenario: Transient Downstream Failure

Trigger

- Temporary network or service glitch

Behavior

- Limited retries with backoff
- Circuit breaker monitors failures

Reasoning

- Recover from short outages
- Avoid traffic amplification
