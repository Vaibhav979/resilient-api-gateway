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
