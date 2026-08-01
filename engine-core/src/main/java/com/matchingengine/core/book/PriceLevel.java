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
        orders.addLast(order);
    }

    public Order peekFirst() {
        return orders.peekFirst();
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    /** Remove and return the oldest order at this level (used once it's fully filled). */
    public Order pollFirst() {
        return orders.pollFirst();
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public BigDecimal totalQuantity() {
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            total = total.add(order.getRemainingQuantity());
        }
        return total;
    }
}