# Mini Exchange — Order Matching Engine

A single-node order matching engine with a REST/WebSocket API, persistence,
and a live dashboard. Built as a scoped-down version of a full exchange
simulation stack — see [`docs/scope.md`](docs/scope.md) for what's in and
what's deliberately left out, and [`docs/plan.md`](docs/plan.md) for the
12-week build plan and role split.

## Modules

| Module | Owner | Description |
|---|---|---|
| [`engine-core`](engine-core) | Person A | Order book, matching logic, concurrency, benchmarking |
| [`api-server`](api-server) | Person B | REST API, WebSocket feed, persistence (SQLite) |
| [`dashboard`](dashboard) | Person B | Live order book + trade feed UI |

## Architecture (target)

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
      SQLite (orders/trades)   WebSocket
                                     │
                                     ▼
                               dashboard (HTML/JS)
```

`engine-core` has no dependency on Spring or any web framework — it's a
plain Java library so it can be unit tested and benchmarked in isolation.
`api-server` depends on `engine-core` and wires it up to HTTP, WebSocket,
and the database.

## Prerequisites

- JDK 21+
- Maven 3.9+

## Building

```bash
mvn -q -pl engine-core,api-server -am install
```

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

## Running the benchmark harness

```bash
cd engine-core
mvn exec:java -Dexec.mainClass="com.matchingengine.core.bench.BenchmarkRunner"
```

## Status

🚧 Skeleton stage — module structure, interfaces, and stub implementations
are in place. See `TODO` markers throughout the code and the issue-shaped
checklist in [`docs/plan.md`](docs/plan.md) for what's left.
