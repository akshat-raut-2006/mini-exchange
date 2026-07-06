-- TODO(person-B): revisit types/precision once the model is finalized.
-- SQLite has no strict decimal type; storing price/quantity as TEXT
-- (decimal string) avoids floating point rounding issues with SQLite's
-- REAL type. Reconsider if this causes friction in queries.

CREATE TABLE IF NOT EXISTS orders (
    id                 TEXT PRIMARY KEY,
    symbol             TEXT NOT NULL,
    side               TEXT NOT NULL,
    type               TEXT NOT NULL,
    price              TEXT,
    quantity           TEXT NOT NULL,
    remaining_quantity TEXT NOT NULL,
    status             TEXT NOT NULL,
    created_at         TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS trades (
    trade_id      TEXT PRIMARY KEY,
    symbol        TEXT NOT NULL,
    maker_order_id TEXT NOT NULL,
    taker_order_id TEXT NOT NULL,
    price         TEXT NOT NULL,
    quantity      TEXT NOT NULL,
    executed_at   TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);
