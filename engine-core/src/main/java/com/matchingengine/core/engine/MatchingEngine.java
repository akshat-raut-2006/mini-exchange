package com.matchingengine.core.engine;

import com.matchingengine.core.book.OrderBook;
import com.matchingengine.core.model.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point for order submission and cancellation. This is the "clean
 * interface" (submit order -> match -> return fills) that api-server calls
 * into — it has zero knowledge of HTTP, WebSocket, or persistence.
 *
 * TODO(person-A): this is the main deliverable. Suggested approach:
 *  1. Get single-threaded correctness working first (no locking, single
 *     caller) with unit tests passing.
 *  2. Then make it thread-safe — likely one lock per symbol's OrderBook,
 *     acquired for the duration of submitOrder/cancelOrder.
 *  3. Then benchmark it (see bench.BenchmarkRunner) and profile before
 *     optimizing further.
 */
public class MatchingEngine {

    private final Map<String, OrderBook> booksBySymbol = new ConcurrentHashMap<>();

    /**
     * Submit a new order. For LIMIT orders this matches against the book
     * and rests any unfilled remainder. For MARKET orders this matches
     * until filled or the book is exhausted (no resting).
     */
    public MatchResult submitOrder(Order order) {
        throw new UnsupportedOperationException("TODO: core matching logic goes here");
    }

    /** Cancel a resting order. Returns true if found and cancelled. */
    public boolean cancelOrder(String symbol, String orderId) {
        throw new UnsupportedOperationException("TODO: delegate to the relevant OrderBook");
    }

    /** Get (or lazily create) the book for a symbol. Package-visible for tests. */
    OrderBook getOrCreateBook(String symbol) {
        return booksBySymbol.computeIfAbsent(symbol, OrderBook::new);
    }
}
