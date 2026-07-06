# Scope

## Cut from the original idea

These add integration risk without adding much interview value at this
project size, so they're explicitly out of scope:

- Custom binary protocol (FIX-lite) — using REST/WebSocket instead
- Kubernetes / container orchestration — a single deployable service is enough
- Full risk-check layer with multiple rule types — keeping just one simple check (later)
- Multi-node distributed matching — single-node engine only

## In scope (the core that matters)

- **Matching engine core** — order book, price-time priority, limit + market
  orders, partial fills, cancel
- **API layer** — REST endpoints to submit/cancel orders, WebSocket feed for
  live order book updates
- **Basic persistence** — trade history and order log (SQLite)
- **Latency/throughput benchmarking** — biggest "wow" factor per hour invested
- **Minimal live dashboard** — order book updating in real time

## Non-goals for v1 (possible stretch goals later)

- Multiple order types beyond limit/market (e.g. stop, iceberg)
- Multi-asset / multi-symbol order books (single symbol is fine for v1)
- Authentication/authorization on the API
- Horizontal scaling of any kind
