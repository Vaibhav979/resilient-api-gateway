# Rate Limiting

## 1. Purpose

The rate limiting mechanism exists to protect the API Gateway and its downstream dependencies from overload caused by excessive or abusive traffic.

Without rate limiting:

- A single client can exhaust request threads
- Tail latency increases for all users
- Downstream services become vulnerable to cascading failure

Rate limiting is a **protective reliability mechanism**, not a correctness requirement.
Its primary goal is to preserve overall system health and fairness under load.

---

## 2. Algorithm Overview

This system uses a **fixed sliding window rate limiting algorithm** enforced at the API
gateway layer.

Policy (current configuration):

- Requests are limited per client identifier (IP address)
- A maximum number of requests is allowed within a fixed time window
- Requests exceeding the limit are rejected

Conceptually:

- The first request from a client starts a time window
- Subsequent requests are counted within that same window
- Once the request count exceeds the threshold, further requests are rejected
- When the window expires, the counter resets automatically

This approach prioritizes simplicity, predictability, and debuggability.

---

## 3. Redis Data Model

Rate limiting state is stored in Redis to allow:

- Shared enforcement across instances
- Atomic updates under concurrency
- Automatic cleanup via expiration

### Key Structure

### Value

- Integer counter representing the number of requests received in the current window

### TTL (Time-To-Live)

- TTL is set equal to the window duration
- TTL defines the lifetime of the rate limit window

Important properties:

- TTL is set **only on the first request** of a window
- TTL is not reset on subsequent requests
- Redis expiration automatically clears the counter when the window ends

This ensures a **fixed window** rather than a continuously sliding one.

---

## 4. Request Flow

### Allowed Request

1. Request arrives at the API Gateway
2. Client identifier (IP address) is extracted
3. Redis counter for the client is atomically incremented
4. If the counter is within the allowed threshold:
   - Request proceeds to the service layer
   - Response is returned with HTTP 200

This path preserves fairness and low latency for compliant clients.

---

### Rejected Request

1. Request arrives within an active window
2. Redis counter exceeds the configured limit
3. The request is rejected immediately
4. API responds with: HTTP 429 Too Many Requests

Rejecting early prevents resource exhaustion and protects other users.

---

## 5. Failure Modes

### Redis Unavailable

**Behavior**

- Rate limiting fails open
- Requests are allowed without enforcement

**Reasoning**

- Rate limiting is a protection mechanism, not a correctness dependency
- Failing closed would cause a full service outage
- Preserving availability is preferred over strict enforcement

**Risk**

- Temporary loss of protection
- Increased risk of overload if abuse coincides with Redis failure

---

### Redis Slow / High Latency

**Behavior**

- Redis calls are bounded by strict timeouts
- Requests fail fast instead of blocking indefinitely
- Rate limiting may be bypassed if Redis times out

**Reasoning**

- Redis slowness is more dangerous than Redis downtime
- Blocking request threads leads to thread pool exhaustion and cascading failure
- Fast failure preserves system recoverability

---

## 6. Trade-offs and Alternatives

### Sliding Window vs Token Bucket

**Chosen**

- Sliding window (Redis-backed)

**Reasoning**

- Simpler mental model
- Easier to debug and reason about
- Adequate for gateway-level protection

**Rejected**

- Token bucket (more complex, higher implementation cost)

---

### Fail-Open vs Fail-Closed

**Chosen**

- Fail-open on Redis failure

**Reasoning**

- Prevents total outage due to dependency failure
- Accepts temporary risk of abuse to preserve availability

**Rejected**

- Fail-closed (guaranteed outage on Redis failure)

---

### Redis vs In-Memory Limiting

**Chosen**

- Redis-backed counters

**Reasoning**

- Shared state across instances
- Consistent enforcement under horizontal scaling

**Rejected**

- In-memory counters (break in distributed systems)

---

## 7. Known Limitations

- Client identification is based on remote IP address
- This is insufficient behind proxies or load balancers
- Forwarded headers are not handled in this version
- No upstream protection (e.g., CDN or load balancer limits)
- Rate limiting can be bypassed during Redis outages
- No differentiation between legitimate spikes and abuse

These limitations are accepted to keep the system focused on core reliability behavior rather than production hardening.

---

## Summary

This rate limiting implementation prioritizes:

- System availability
- Predictable behavior
- Explicit failure handling
- Clear engineering trade-offs

The design intentionally favors loud, observable behavior over silent degradation and documents accepted risks rather than hiding them.
