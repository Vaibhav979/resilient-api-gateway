# Postmortem: Stale Cache Incident - API Gateway Downstream Service Failure

**Date:** 2026-02-15  
**Duration:** ~2 hours  
**Severity:** SEV-2 (Degraded Service behavior)
**Status:** Resolved

---

## Summary

The API Gateway was returning "Downstream unavailable" error responses to all users even though the downstream service was fully operational and reachable from within the Kubernetes cluster. The system was incorrectly returning degraded/fallback responses due to a combination of configuration issues and stale cache behavior.

---

## Impact

### User Impact

- All users calling `GET /api/data` received error responses
- Error message: `{"status":"degraded","message":"Downstream unavailable","source":"fallback"}`
- Users experienced complete service unavailability despite the service being functional

### System Impact

- Circuit breaker transitioned to **OPEN** state with 100% failure rate
- Retry mechanism was misconfigured and not functioning properly
- Database was serving stale cached data without validation
- System metrics showed 6 failed calls with 0 successful calls

---

## Detection

1. **User Reports:** Users reported inability to access downstream data through the API Gateway
2. **Circuit Breaker Metrics:** Prometheus metrics showed circuit breaker in OPEN state

```
   resilience4j_circuitbreaker_state{state="open"} = 1.0
   resilience4j_circuitbreaker_failure_rate = 100.0

```

3. **Internal Verification:** Direct test from within the pod confirmed downstream service was working:

```
bash
   kubectl exec -it deployment/api-gateway -- wget -qO- http://downstream:8080/downstream/data
   # Returns: {"status":"success",...}

```

---

## Root Cause

### Primary Root Cause

The API Gateway was reading the downstream URL from the wrong property name. The code used:

```
java
@Value("${downstream.url:http://localhost:8081/downstream/data}")
```

But Kubernetes was setting the environment variable `DOWNSTREAM_URL`. Environment variable binding behaved differently when accessed via @Value compared to configuration property binding, leading to unexpected fallback to default values.

### Secondary Root Cause - Retry Misconfiguration

The retry configuration name didn't match the code:

- **application.yml:** `downstreamRetry`
- **Code:** `retryRegistry.retry("downstreamService")`

This caused the custom retry settings (max-attempts: 3, etc.) to not be applied.

### Tertiary Root Cause - Stale Database Cache

The database was returning stale cached responses without validating freshness:

- When Redis cache missed, the system checked the database
- Database had old cached responses from previous failed states
- These stale responses were returned to users without ever calling the downstream service
- This behavior masked downstream availability and prevented recovery signals from propagating through resilience mechanisms

---

## Contributing Factors

1. **Configuration Mismatch:** Environment variable naming inconsistency between Kubernetes manifests and application code
2. **Improper Cache Validation:** Database cache had no time-to-live or freshness check
3. **Silent Failures:** Circuit breaker and retry failures were not visible in standard logs
4. **Multi-Layer Caching Complexity:** Three-tier caching (Redis → Database → Downstream) with unclear failure handling
5. **Missing Debugging Instrumentation:** Lack of startup logs showing the configured downstream URL

---

## Resolution

### Fix 1: Environment Variable Mapping

**File:** `api_gateway/src/main/java/com/infra/api_gateway/clients/DownStreamClient.java`

Changed:

```
java
@Value("${downstream.url:http://localhost:8081/downstream/data}")
```

To:

```
java
@Value("${DOWNSTREAM_URL:http://localhost:8081/downstream/data}")
```

### Fix 2: Retry Configuration

**File:** `api_gateway/src/main/resources/application.yml`

Changed:

```
yaml
retry:
  instances:
    downstreamRetry:
```

To:

```
yaml
retry:
  instances:
    downstreamService:
```

### Fix 3: Database Cache Age Check

**File:** `api_gateway/src/main/java/com/infra/api_gateway/clients/DownStreamClient.java`

Added maximum age validation for database cache:

```
java
private static final Duration MAX_DB_CACHE_AGE = Duration.ofSeconds(30);

// When reading from database:
if (cacheAgeSeconds > MAX_DB_CACHE_AGE.getSeconds()) {
    // Ignore stale cache, call downstream instead
}
```

---

## Prevention/Action Items

- [ ] Add startup validation to log the resolved downstream URL
- [ ] Implement cache TTL enforcement for all cache layers
- [ ] Add health check endpoint that verifies downstream connectivity
- [ ] Create alerting for circuit breaker state changes
- [ ] Document environment variable naming conventions
- [ ] Add integration tests for configuration property binding
- [ ] Implement "cache age" metrics to monitor stale data

---

## Key Lessons Learned

1. **Environment Variable Binding:** @Value annotations require exact environment variable names - Spring Boot's relaxed binding only works for properties files, not @Value
2. **Cache Can Harm:** Stale cache can completely hide service availability - always validate freshness
3. **Multi-Layer Caching Risk:** Each cache layer adds complexity and potential failure modes
4. **Internal Verification ≠ External Access:** Just because a service works inside the cluster doesn't mean it's accessible from all paths
5. **Configuration Testing:** Always test configuration loading at startup with visible logging

---

## Reliability Principle Added

**Cache Freshness Requirement:** All cached data must include timestamp/age validation before serving to users. No cache layer should return data older than the defined TTL without revalidation from the source.

---

## Verification Results

After fixes applied:

```
$ curl http://localhost:8080/api/data
{"delayMs":0,"servedAt":"2026-02-15T04:42:16Z","status":"success","source":"downstream","value":"dummy-data"}

Circuit Breaker State: CLOSED
Successful Calls: 1
Failure Rate: -1.0 (no failures)
```
