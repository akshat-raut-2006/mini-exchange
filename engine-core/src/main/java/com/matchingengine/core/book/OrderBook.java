package com.matchingengine.core.book;

import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.OrderSide;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Single-symbol order book: two price-ordered maps of {@link PriceLevel},
 * one per side.
 *
 * TODO(person-A):
 *  - bids should iterate highest price first, asks lowest price first
 *    (consider Comparator.reverseOrder() for the bid map)
 *  - decide where thread-safety lives: here, or one lock per symbol held by
 *    the engine? Keeping it here (e.g. a single ReentrantLock per book) is
 *    the simplest correct starting point.
 *  - maintain an orderId -> Order index for O(1) cancel lookups
 */
public class OrderBook {

    private final String symbol;

    // TODO: bids should sort descending (best bid = highest price first)
    private final Map<BigDecimal, PriceLevel> bids = new TreeMap<>();
    // asks sort ascending (best ask = lowest price first) — TreeMap's natural order is fine here
    private final Map<BigDecimal, PriceLevel> asks = new TreeMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Add a resting (unmatched or partially matched) limit order to the book. */
    public void addOrder(Order order) {
        throw new UnsupportedOperationException("TODO: insert into the correct side/price level");
    }

    /** Remove an order from the book (cancel). Returns true if it was found and removed. */
    public boolean cancelOrder(String orderId) {
        throw new UnsupportedOperationException("TODO: look up order by id, remove from its price level");
    }

    /** Best bid price, or null if the bid side is empty. */
    public BigDecimal bestBid() {
        throw new UnsupportedOperationException("TODO: highest price with resting buy orders");
    }

    /** Best ask price, or null if the ask side is empty. */
    public BigDecimal bestAsk() {
        throw new UnsupportedOperationException("TODO: lowest price with resting sell orders");
    }

    /** Snapshot of price levels for one side, best price first. Used by the API/dashboard. */
    public List<PriceLevel> getLevels(OrderSide side) {
        throw new UnsupportedOperationException("TODO: return ordered snapshot of bids or asks");
    }
}
