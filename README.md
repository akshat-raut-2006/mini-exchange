# Mini Exchange — Order Matching Engine

A single-node stock exchange, built from scratch: a real order-matching
engine, a multi-symbol live order book, authenticated trading, and a
trading-terminal-style dashboard — all backed by a persistent database.
Built as a portfolio project to demonstrate systems design, concurrency,
and full-stack integration, not just a CRUD app with a UI on top.

## What it does

- **Matches real orders.** Submit LIMIT or MARKET orders on either side
  (BUY/SELL); the engine matches against resting orders using price-time
  priority, the same core algorithm real exchanges use.
- **Multiple symbols, isolated books.** RELIANCE, TCS, INFY, AAPL, TSLA —
  each trades against its own independent order book.
- **Authenticated, tracked trading.** Sign up / sign in via Supabase Auth;
  every order and trade is tied to the account that placed it.
- **Live, not polled.** The dashboard updates over WebSocket as the book
  changes — depth ladder, trade tape ("time & sales"), spread, and
  last-price all update in real time.
- **Actually persisted.** Every order and trade is written to a Postgres
  database (Supabase), not just held in memory.
- **Benchmarked.** See [Performance](#performance) below for real
  throughput/latency numbers, not estimates.

## Demo

<!--
  Add your demo video here. Easiest way: open this file in the GitHub web
  editor (click the pencil icon on README.md in the repo), then drag the
  video file directly into the edit box. GitHub uploads it and inserts a
  link automatically - replace the line below with whatever it generates
  (it'll look like a markdown link to https://github.com/user-attachments/...).
  Works for .mp4/.mov, renders as an inline playable video on the repo page.
-->
*Video coming soon.*

## Screenshots

<!-- Same idea - drag image files into the GitHub web editor, or reference
     files you've added under a docs/screenshots/ folder, e.g.:
     ![Trading terminal](docs/screenshots/terminal.png) -->

## Architecture

```
                 ┌─────────────┐
   HTTP  ───────▶│             │
  (submit/cancel)│  api-server │──────▶ engine-core (in-process)
                 │  (Spring    │              │
                 │   Boot)     │◀─────────────┘ fills / book deltas
                 │             │
                 └──────┬──────┘
                        │
             ┌──────────┴───────────┐
             ▼                      ▼
    Supabase/Postgres          WebSocket
    (orders/trades, auth)            │
                                      ▼
                                dashboard (HTML/JS)
```

`engine-core` has no dependency on Spring or any web framework — it's a
plain Java library, so it can be unit tested and benchmarked in complete
isolation from the API layer. `api-server` depends on `engine-core` and
wires it up to HTTP, WebSocket, authentication, and the database.

**Auth flow:** the dashboard authenticates directly against Supabase Auth
and gets back a JWT. `api-server` verifies that token on every order
submission against Supabase's public key (JWKS) — it never touches a
password, and Supabase never touches the matching engine.

## Modules

| Module | Description |
|---|---|
| [`engine-core`](engine-core) | Order book, matching logic, benchmarking |
| [`api-server`](api-server) | REST API, WebSocket feed, auth, persistence |
| [`dashboard`](dashboard) | Live trading terminal UI |

## Prerequisites

- JDK 21+
- Maven 3.9+
- A Supabase project (free tier is fine) — see [Database setup](#database-setup)

## Building

```bash
mvn -q -pl engine-core,api-server -am install
```

## Database setup

`api-server` persists orders and trades to Supabase Postgres via plain
JDBC (`schema.sql` runs automatically on startup).

1. Create a project at [supabase.com](https://supabase.com).
2. **Project Settings → Database** — copy the connection string (prefer
   the **Transaction pooler** URI, port 6543).
3. Create `api-server/.env` (already gitignored — never commit this file):

   ```
   SUPABASE_DB_URL=jdbc:postgresql://<your-pooler-host>:6543/postgres?sslmode=require
   SUPABASE_DB_USER=<your-db-user>
   SUPABASE_DB_PASSWORD=<your-db-password>
   ```

   `.env` is loaded automatically on startup (via `spring-dotenv`) — no
   manual `export` needed.

## Running

```bash
cd api-server
mvn spring-boot:run
```

Then open `dashboard/index.html` in a browser (it points at
`http://localhost:8080` by default — see `dashboard/js/app.js`).

## Running tests

```bash
mvn test
```

## Performance

Run the benchmark harness yourself:

```bash
cd engine-core
mvn exec:java -Dexec.mainClass="com.matchingengine.core.bench.BenchmarkRunner"
```

| Scenario | Throughput | p50 | p99 | p99.9 | Max |
|---|---|---|---|---|---|
| Single-threaded | 1,362,635 orders/sec | 0.33 µs | 3.63 µs | 9.63 µs | 9,790 µs* |
| Concurrent (4 threads, 4 symbols) | 1,926,430 orders/sec | 0.54 µs | 20.42 µs | 107.42 µs | 16,562 µs* |

\* A single outlier per run, almost certainly a JIT/GC pause — note it's
3+ orders of magnitude above p99.9, not representative of typical latency.

Throughput scales sub-linearly under concurrency (1.4x, not 4x, for 4
threads) despite each thread trading a fully isolated symbol — this
reflects JVM-wide costs (GC, cache contention) that don't disappear just
because the books themselves don't share state.

## Known limitations

- A single symbol's `OrderBook` is not yet thread-safe for concurrent
  writers (plain `TreeMap`/`HashMap`, no locking). Different symbols are
  safely isolated via a per-symbol map, so this only matters for
  concurrent access to *one* book — see the benchmark's design note for
  more detail. The natural next step is a per-symbol lock.
- Self-trade prevention is not yet implemented.

## Roadmap

- [ ] Full design write-up (`docs/design-writeup.md`)
- [ ] Per-symbol locking for concurrent single-book writes
- [ ] Self-trade prevention policy
