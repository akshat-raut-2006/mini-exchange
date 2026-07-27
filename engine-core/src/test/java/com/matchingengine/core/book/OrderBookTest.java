package com.matchingengine.core.book;

import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.OrderSide;
import com.matchingengine.core.model.OrderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO(person-A): flesh these out as OrderBook is implemented. Suggested
 * cases beyond the ones stubbed below: adding multiple orders at the same
 * price preserves FIFO order; cancelling the only order at a level removes
 * the level entirely; best bid/ask update correctly as levels are added/removed.
 */
class OrderBookTest {

    private static Order limit(String id, OrderSide side, String price, String qty, long seq) {
        return new Order(id, "BTC-USD", side, OrderType.LIMIT, new BigDecimal(price), new BigDecimal(qty), seq);
    }

    @Test
    void addingOrderMakesItBestBidOrAsk() {
        OrderBook book = new OrderBook("BTC-USD");

        book.addOrder(limit("b1", OrderSide.BUY, "100.00", "1", 1));
        assertEquals(new BigDecimal("100.00"), book.bestBid());
        assertNull(book.bestAsk());

        book.addOrder(limit("a1", OrderSide.SELL, "101.00", "1", 2));
        assertEquals(new BigDecimal("101.00"), book.bestAsk());

        // a better bid should become the new best bid
        book.addOrder(limit("b2", OrderSide.BUY, "100.50", "1", 3));
        assertEquals(new BigDecimal("100.50"), book.bestBid());

        // a better (lower) ask should become the new best ask
        book.addOrder(limit("a2", OrderSide.SELL, "100.75", "1", 4));
        assertEquals(new BigDecimal("100.75"), book.bestAsk());
    }

    @Test
    void cancellingOrderRemovesItFromBook() {
        OrderBook book = new OrderBook("BTC-USD");
        book.addOrder(limit("b1", OrderSide.BUY, "100.00", "1", 1));

        assertTrue(book.cancelOrder("b1"));
        assertNull(book.bestBid());
        assertFalse(book.cancelOrder("b1")); // already gone
        assertFalse(book.cancelOrder("does-not-exist"));
    }

    @Test
    void ordersAtSamePriceMaintainFifoOrder() {
        OrderBook book = new OrderBook("BTC-USD");
        Order first = limit("b1", OrderSide.BUY, "100.00", "1", 1);
        Order second = limit("b2", OrderSide.BUY, "100.00", "1", 2);

        book.addOrder(first);
        book.addOrder(second);

        List<PriceLevel> levels = book.getLevels(OrderSide.BUY);
        assertEquals(1, levels.size());
        assertEquals(first, levels.get(0).peekFirst()); // oldest order stays at the front
    }

    @Test
    void emptyBookHasNullBestBidAndAsk() {
        OrderBook book = new OrderBook("BTC-USD");
        assertNull(book.bestBid());
        assertNull(book.bestAsk());
        assertTrue(book.getLevels(OrderSide.BUY).isEmpty());
        assertTrue(book.getLevels(OrderSide.SELL).isEmpty());
    }
}