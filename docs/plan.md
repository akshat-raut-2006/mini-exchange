# Plan

~300 hours total (2 people × ~150 hrs, ~12-13 hrs/week over 12 weeks).

## Roles

**Person A — Core Engine & Performance** (`engine-core`)
- Order book data structure + matching logic
- Concurrency handling (thread-safe order submission)
- Benchmarking harness (throughput, p99 latency)
- Unit tests for matching correctness (partial fills, self-trades,
  price-time ordering)

**Person B — API, Persistence & Dashboard** (`api-server`, `dashboard`)
- REST API (submit/cancel orders) + WebSocket broadcaster for order book deltas
- Persistence layer (SQLite) for trades/orders
- Frontend dashboard (live order book + trade feed)
- Deployment (single server / documented local run + demo video)

Independent for the first half, integrating in the second half.

## Week-by-week

- **Weeks 1-2 — Design + skeleton**
  - Both: agree on language (✅ Java)
  - A: design order book data structure (price-ordered levels, each a FIFO
    queue of orders)
  - B: scaffold API server + database schema
- **Weeks 3-5 — Core matching logic**
  - A: implement matching algorithm — limit, market, partial fills, cancels.
    Tests as you go.
  - B: wire API endpoints to a stub engine, get persistence working
    end-to-end with dummy data
- **Weeks 6-7 — Integration**
  - A: expose engine via clean interface (submit order → match → return fills)
  - B: connect real API to real engine, get WebSocket broadcasting live
    book updates
  - Both: first working end-to-end demo
- **Weeks 8-9 — Dashboard + polish**
  - B: build the live dashboard (depth view + recent trades feed)
  - A: start benchmarking — throughput and latency under load
- **Weeks 10-11 — Hardening + benchmarking depth**
  - A: stress test, fix concurrency bugs, get quotable numbers
  - B: basic API tests, dashboard polish, README + architecture diagram
- **Week 12 — Buffer + demo prep**
  - Demo video, final write-up of design decisions and trade-offs

## Checklist

- [ ] `Order`, `Trade`, enums finalized (`engine-core/model`)
- [ ] `OrderBook` add/cancel/match implemented
- [ ] `MatchingEngine` thread-safety strategy decided and implemented
- [ ] Unit tests: partial fill, self-trade, price-time priority, cancel
- [ ] `BenchmarkRunner` producing throughput + p50/p99 latency numbers
- [ ] REST endpoints: submit, cancel, get book snapshot
- [ ] WebSocket broadcasting book deltas
- [ ] SQLite schema + repository for orders/trades
- [ ] Dashboard renders live book + trade feed
- [ ] README with architecture diagram + design trade-offs write-up
- [ ] Demo video recorded
