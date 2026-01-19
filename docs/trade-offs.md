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
