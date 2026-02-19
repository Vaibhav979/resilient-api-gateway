# Project Closure — Resilient System

## Objective

Build a resilient backend platform demonstrating caching, rate limiting,
fault tolerance, observability, and GitOps deployment.

---

## What Was Built

- API Gateway with resilience patterns
- Mock Downstream Service to simulate service behavior under load
- Redis + DB caching
- Sliding window rate limiter
- Circuit breaker & retries
- Full observability stack (Prometheus + Grafana)
- GitOps deployment using ArgoCD on Kubernetes

---

## Major Challenges

- Local resource constraints running Kubernetes
- Understanding Prometheus metric behavior
- Debugging empty Grafana dashboards
- CI/CD image publishing issues
- Metric interpretation vs metric existence

---

## Key Learnings

- Observability shows _change_, not existence
- Distributed systems fail in layers
- GitOps simplifies deployment consistency
- Resilience must be tested intentionally