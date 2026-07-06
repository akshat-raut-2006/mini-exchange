# Design Write-up (fill in during week 12)

Interviewers love asking "why did you choose X over Y" — use this doc to
capture those decisions as you make them, rather than reconstructing them
from memory at the end.

## Order book data structure

- What did you use (e.g. `TreeMap<BigDecimal, PriceLevel>`), and why over
  the alternatives (sorted list, skip list, heap)?
- What's the complexity of add/cancel/best-price lookup?

## Concurrency model

- Where does the lock live (per-book, per-engine, lock-free)?
- What did you try first, and what changed after benchmarking?

## Self-trade policy

- What happens when an incoming order would match against the same
  participant's resting order? What did you implement, and why?

## Benchmark results

- Throughput: ___ orders/sec
- Latency: p50 ___ ms, p99 ___ ms
- What was the bottleneck you found, and what did fixing it buy you?
  (e.g. "lock contention on the order book → fixed by X → 3x throughput")

## What you'd do differently at 10x the scope/time

- Multi-symbol support?
- Real risk checks?
- Persistence beyond SQLite?
