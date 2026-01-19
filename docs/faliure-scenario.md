# Failure Scenarios

## Scenario: Client sends invalid request (wrong endpoint or method)

**Expected Behavior:**

- Server returns `404 Not Found` or `405 Method Not Allowed`
- Request does not reach business logic
- No retries are performed
- No alerts are triggered

**Rationale:**
These errors indicate incorrect client behavior and do not represent system unreliability.

## Scenario: Redis Unavailable

**Trigger**

- Redis container stopped or unreachable

**Expected Behavior**

- API fails fast with 503 Service Unavailable

**User Impact**

- Requests rejected quickly
- No hanging connections

**Reasoning**

- Prevent thread exhaustion
- Make dependency failure explicit

### Scenario: Rate Limit Exceeded

**Trigger**
- Client exceeds request quota within time window

**Expected Behavior**
- API returns 429 Too Many Requests

**User Impact**
- Client must retry later
- Other users unaffected

**Reasoning**
- Protect system from overload
