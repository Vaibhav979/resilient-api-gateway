## Problem Statement

Build a resilient API gateway that protects downstream services
from traffic spikes and partial failures.

## System Goals

- Enforce rate limiting per client
- Fail gracefully under dependency outages
- Expose reliability metrics

## Non-Goals

- Authentication / Authorization
- Frontend UI
- Business logic complexity
- Kubernetes orchestration

## High-Level Architecture

Clients → API Gateway → (Redis / Downstream Service / DB)

## Request Flow

1. Request arrives at API Gateway
2. Rate limit check is performed
3. Request is either rejected or processed
4. Response returned to client

### Error Responsibility & Failure Signaling

- This service explicitly differentiates between client-side failures (4xx) and server-side failures (5xx) to preserve observability correctness and error budget integrity.

- Client errors (e.g., invalid requests, unsupported endpoints, rate limit violations) are treated as contract violations and returned as 4xx responses. These failures do not indicate service unreliability and do not consume error budget.

- Server errors (5xx) represent failures in handling valid requests and are returned only for unexpected internal conditions or dependency failures. The system prefers fast failure over retries in these cases to avoid cascading failures and thread exhaustion.
