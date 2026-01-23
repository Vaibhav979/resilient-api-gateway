### Rate Limiting Algorithm

**Chosen**

- Redis-backed sliding window

**Alternatives**

- Token bucket
- Leaky bucket

**Reasoning**

- Simpler implementation
- Easy to debug
- Adequate for gateway protection

### Rate Limiting Failure Strategy

**Decision**

- Fail-open when Redis is unavailable

**Reasoning**

- Rate limiting is a protective mechanism, not a correctness requirement
- Failing closed would cause full service outage
- Temporary loss of protection is preferable to total unavailability

**Risk**

- Potential overload if abuse coincides with Redis outage

**Mitigation**

- Short Redis timeouts
- Monitoring Redis health
