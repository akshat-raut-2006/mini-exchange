package com.matchingengine.core.book;

import com.matchingengine.core.model.Order;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * All resting orders at a single price, in time priority (FIFO).
 *
 * TODO(person-A): this is the core data structure question for week 1 —
 * confirm ArrayDeque is the right choice vs. a LinkedList, and whether
 * cancel-by-id needs an auxiliary index (probably yes, see OrderBook).
 */
public class PriceLevel {

    private final BigDecimal price;
    private final Deque<Order> orders = new ArrayDeque<>();

    public PriceLevel(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void addOrder(Order order) {
        throw new UnsupportedOperationException("TODO: append to back of queue (time priority)");
    }

    public Order peekFirst() {
        throw new UnsupportedOperationException("TODO: return oldest order at this level without removing it");
    }

    public void removeOrder(Order order) {
        throw new UnsupportedOperationException("TODO: remove a specific order (cancel), keep FIFO order intact");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO: true when no resting orders remain at this level");
    }

    public BigDecimal totalQuantity() {
        throw new UnsupportedOperationException("TODO: sum of remaining quantity across all orders at this level");
    }
}
