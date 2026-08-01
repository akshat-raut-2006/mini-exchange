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
| [`api-server`](api-server) | Person B | REST API, WebSocket feed, persistence (Supabase/Postgres) |
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
    Supabase/Postgres          WebSocket
       (orders/trades)
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

## Database (Supabase)

`api-server` persists orders and trades to a Supabase Postgres database via
plain JDBC (`schema.sql` is applied automatically on startup). To connect:

1. Create a project at [supabase.com](https://supabase.com) (or use an
   existing one).
2. Go to **Project Settings → Database** and copy the connection string.
   Prefer the **Transaction pooler** URI (port 6543) for deployment - it
   works well with the small connection pool used here. The direct
   connection (port 5432) also works for local development.
3. Export the connection details as environment variables before running
   the server:

   ```bash
   export SUPABASE_DB_URL="jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres?sslmode=require"
export SUPABASE_DB_USER="postgres.vxibkcerkykulnmedvcu"
export SUPABASE_DB_PASSWORD="AkshatRaut19/7/6"
   ```

   (Or put them in a `.env` file and source it - just don't commit it.)

On startup, Spring runs `schema.sql` against that database, creating the
`orders` and `trades` tables if they don't already exist.

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