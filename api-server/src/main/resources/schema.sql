-- TODO(person-B): revisit types/precision once the model is finalized.
-- Storing price/quantity as TEXT (decimal string) avoids floating point
-- rounding issues. Reconsider if this causes friction in queries.

CREATE TABLE IF NOT EXISTS orders (
    id                 TEXT PRIMARY KEY,
    symbol             TEXT NOT NULL,
    side               TEXT NOT NULL,
    type               TEXT NOT NULL,
    price              TEXT,
    quantity           TEXT NOT NULL,
    remaining_quantity TEXT NOT NULL,
    status             TEXT NOT NULL,
    created_at         TEXT NOT NULL,
    user_id            TEXT,
    user_email         TEXT
);

-- The orders table already exists in Supabase from before auth was added,
-- so CREATE TABLE IF NOT EXISTS above is a no-op there - these ALTERs are
-- what actually add the two new columns to the live table.
ALTER TABLE orders ADD COLUMN IF NOT EXISTS user_id TEXT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS user_email TEXT;

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
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);
