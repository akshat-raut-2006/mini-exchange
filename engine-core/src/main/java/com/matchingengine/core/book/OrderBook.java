package com.matchingengine.core.book;

import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.OrderSide;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

    // bids sort descending (best bid = highest price first)
    private final TreeMap<BigDecimal, PriceLevel> bids = new TreeMap<>(Comparator.reverseOrder());
    // asks sort ascending (best ask = lowest price first) — TreeMap's natural order is fine here
    private final TreeMap<BigDecimal, PriceLevel> asks = new TreeMap<>();

    // orderId -> order, for O(1) cancel lookups
    private final Map<String, Order> ordersById = new HashMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Add a resting (unmatched or partially matched) limit order to the book. */
    public void addOrder(Order order) {
        if (order.getPrice() == null) {
            throw new IllegalArgumentException("Cannot rest an order with no price (market order): " + order);
        }
        Map<BigDecimal, PriceLevel> side = sideMap(order.getSide());
        PriceLevel level = side.computeIfAbsent(order.getPrice(), PriceLevel::new);
        level.addOrder(order);
        ordersById.put(order.getId(), order);
    }

    /** Remove an order from the book (cancel). Returns true if it was found and removed. */
    public boolean cancelOrder(String orderId) {
        Order order = ordersById.remove(orderId);
        if (order == null) {
            return false;
        }
        Map<BigDecimal, PriceLevel> side = sideMap(order.getSide());
        PriceLevel level = side.get(order.getPrice());
        if (level == null) {
            return false;
        }
        level.removeOrder(order);
        if (level.isEmpty()) {
            side.remove(order.getPrice());
        }
        return true;
    }

    /** Look up a resting order by id, or null if it isn't (or is no longer) on the book. */
    public Order getOrder(String orderId) {
        return ordersById.get(orderId);
    }

    /** Best bid price, or null if the bid side is empty. */
    public BigDecimal bestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    /** Best ask price, or null if the ask side is empty. */
    public BigDecimal bestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    /**
     * The best (highest-priority) resting order on a side — i.e. the order at the front
     * of the best price level — or null if that side is empty. Used by the matching
     * engine to find the next maker to match against. Does not remove anything.
     */
    public Order peekBest(OrderSide side) {
        Map.Entry<BigDecimal, PriceLevel> best = sideMap(side).firstEntry();
        return best == null ? null : best.getValue().peekFirst();
    }

    /**
     * Called by the matching engine after it has matched against {@code makerOrder} and
     * reduced its remaining quantity. If the maker is now fully filled, removes it from
     * its price level (and removes the level itself if now empty) and drops it from the
     * id index. If the maker still has quantity remaining, this is a no-op — it stays at
     * the front of its level, which is correct since it's the oldest order there.
     */
    public void syncAfterMatch(Order makerOrder) {
        if (makerOrder.getRemainingQuantity().signum() > 0) {
            return;
        }
        Map<BigDecimal, PriceLevel> side = sideMap(makerOrder.getSide());
        PriceLevel level = side.get(makerOrder.getPrice());
        if (level == null) {
            return;
        }
        level.removeOrder(makerOrder);
        ordersById.remove(makerOrder.getId());
        if (level.isEmpty()) {
            side.remove(makerOrder.getPrice());
        }
    }

    /** Snapshot of price levels for one side, best price first. Used by the API/dashboard. */
    public List<PriceLevel> getLevels(OrderSide side) {
        return new ArrayList<>(sideMap(side).values());
    }

    private TreeMap<BigDecimal, PriceLevel> sideMap(OrderSide side) {
        return side == OrderSide.BUY ? bids : asks;
    }
}