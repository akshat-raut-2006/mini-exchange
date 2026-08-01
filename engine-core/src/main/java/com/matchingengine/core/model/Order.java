package com.matchingengine.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A single order. Mutable with respect to {@code remainingQuantity} and
 * {@code status} as it gets matched — everything else is immutable once
 * created.
 *
 * TODO(person-A): decide whether mutation happens in place (guarded by the
 * book's lock) or whether matching produces new immutable snapshots. Mutable
 * order objects are simplest but the concurrency story needs to be explicit.
 */
public class Order {

    private final String id;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final BigDecimal price;       // null for MARKET orders
    private final BigDecimal quantity;    // original quantity
    private final Instant createdAt;
    private final long sequence;          // tie-breaker for price-time priority

    private BigDecimal remainingQuantity;
    private OrderStatus status;

    public Order(String id, String symbol, OrderSide side, OrderType type,
                 BigDecimal price, BigDecimal quantity, long sequence) {
        this.id = Objects.requireNonNull(id);
        this.symbol = Objects.requireNonNull(symbol);
        this.side = Objects.requireNonNull(side);
        this.type = Objects.requireNonNull(type);
        this.price = price;
        this.quantity = Objects.requireNonNull(quantity);
        this.remainingQuantity = quantity;
        this.status = OrderStatus.NEW;
        this.createdAt = Instant.now();
        this.sequence = sequence;
    }

    public String getId() { return id; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getQuantity() { return quantity; }
    public Instant getCreatedAt() { return createdAt; }
    public long getSequence() { return sequence; }

    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public OrderStatus getStatus() { return status; }

    /** TODO(person-A): call this from the matching engine, not from outside callers. */
    public void reduceRemaining(BigDecimal filledQty) {
        if (filledQty == null || filledQty.signum() < 0) {
            throw new IllegalArgumentException("filledQty must be non-negative: " + filledQty);
        }
        if (filledQty.compareTo(remainingQuantity) > 0) {
            throw new IllegalArgumentException(
                    "filledQty %s exceeds remainingQuantity %s".formatted(filledQty, remainingQuantity));
        }
        remainingQuantity = remainingQuantity.subtract(filledQty);
        if (remainingQuantity.signum() == 0) {
            status = OrderStatus.FILLED;
        } else {
            status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public void markCancelled() {
        if (status == OrderStatus.FILLED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in status " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "Order{id=%s, symbol=%s, side=%s, type=%s, price=%s, qty=%s, remaining=%s, status=%s}"
                .formatted(id, symbol, side, type, price, quantity, remainingQuantity, status);
    }
}