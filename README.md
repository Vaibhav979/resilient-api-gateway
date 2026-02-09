## 📌 Overview

This project implements a fault-tolerant API Gateway designed to handle
partial failures in distributed systems.

## 🏗 Architecture

Client → Gateway → Redis → PostgreSQL → Downstream Service

## ⚙️ Tech Stack

Java, Spring Boot, Redis, PostgreSQL, Docker, Resilience4j

## ✨ Features

- Multi-layer caching
- Sliding window rate limiter
- Circuit breakers
- Automatic fallback
- Dockerized deployment

## 🚀 Setup

docker-compose up --build

## 🛡 Resilience Testing

- Simulated Redis failure
- Simulated DB outage
- Load testing

## Reliability

See detailed SLOs, alerts, and error budgets in:

📄 docs/reliability.md
