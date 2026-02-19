## 📌 Overview

This project demonstrates a production-style resilient backend system built using modern backend, DevOps, and GitOps practices.
The system simulates real-world service behavior under load, failures, and recovery while providing full observability through metrics and dashboards.

The goal was to design and operate a system that is not only functional but also resilient, observable, and deployable using cloud-native workflows.

---

## 🏗 Architecture

Client → Gateway → Redis → PostgreSQL → Downstream Service

API Gateway → Actuator → Prometheus → Grafana

Developer → GitHub
↓
GitHub Actions (CI)
↓
Docker Registry (GHCR)
↓
Git Repo (K8s manifests)
↓
ArgoCD
↓
Kubernetes Cluster (k3s)
↓
Services + Monitoring Stack

---

## ⚙️ Tech Stack

Java, Spring Boot, Redis, PostgreSQL, Docker, Resilience4j, Kubernetes, GitHub Actions, Prometheus, Grafana, ArgoCD

---

## Key Features

### Backend Engineering

- API Gateway architecture (Spring Boot)
- Redis caching layer
- Database caching strategy
- Sliding window rate limiting
- Downstream service communication
- Failure injection for resilience testing

### Resilience Patterns

- Circuit Breaker (Resilience4j)
- Retry mechanisms
- Rate limiting protection
- Graceful degradation

### Observability

- Micrometer metrics exposure
- Custom business metrics
- p90 / p95 / p99 latency tracking
- JVM & system monitoring
- Prometheus metrics scraping
- Grafana real-time dashboards

### DevOps & GitOps

- CI pipeline with automated builds
- Docker image creation & registry push
- Kubernetes deployment (k3s)
- ArgoCD GitOps reconciliation
- Declarative infrastructure management

---

## What This Project Demonstrates

- End-to-end system lifecycle
- Backend + Platform engineering integration
- Observability-driven development
- Failure-aware system design
- GitOps deployment workflow

## 🛡 Resilience Testing

- Simulated Redis failure
- Simulated DB outage
- Load testing

## Reliability

See detailed SLOs, alerts, and error budgets in:

📄 docs/reliability.md
