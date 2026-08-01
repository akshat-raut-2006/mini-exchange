package com.matchingengine.core.engine;

import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.OrderSide;
import com.matchingengine.core.model.OrderStatus;
import com.matchingengine.core.model.OrderType;
import com.matchingengine.core.model.Trade;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO(person-A): this is the test file that matters most for the "unit
 * tests for matching correctness" deliverable — interviewers will ask
 * about these cases specifically. Implement in roughly this order:
 *  1. simple full fill (one resting order, one incoming order, same qty)
 *  2. partial fill (incoming order larger/smaller than resting order)
 *  3. price-time priority (two resting orders at same price, oldest fills first)
 *  4. price priority (better price fills before worse price on the other side)
 *  5. self-trade handling (decide + document the policy: allow, reject, or
 *     cancel one side — this is exactly the kind of design decision
 *     interviewers ask "why did you choose X" about)
 *  6. market order consumes multiple price levels if needed
 *  7. cancel removes a resting order and it no longer matches
 */
class MatchingEngineTest {

    private static Order limit(String id, OrderSide side, String price, String qty, long seq) {
        return new Order(id, "BTC-USD", side, OrderType.LIMIT, new BigDecimal(price), new BigDecimal(qty), seq);
    }

    private static Order market(String id, OrderSide side, String qty, long seq) {
        return new Order(id, "BTC-USD", side, OrderType.MARKET, null, new BigDecimal(qty), seq);
    }

    @Test
    void fullyMatchesEqualQuantityLimitOrders() {
        MatchingEngine engine = new MatchingEngine();
        engine.submitOrder(limit("resting", OrderSide.SELL, "100.00", "1", 1));

        MatchResult result = engine.submitOrder(limit("incoming", OrderSide.BUY, "100.00", "1", 2));

        assertEquals(1, result.trades().size());
        Trade trade = result.trades().get(0);
        assertEquals(new BigDecimal("100.00"), trade.price());
        assertEquals(new BigDecimal("1"), trade.quantity());
        assertEquals(OrderStatus.FILLED, result.incomingOrder().getStatus());
        assertNull(engine.getBook("BTC-USD").bestAsk()); // resting order fully consumed
        assertNull(engine.getBook("BTC-USD").bestBid()); // nothing left resting either
    }

    @Test
    void partiallyFillsWhenIncomingQuantityExceedsResting() {
        MatchingEngine engine = new MatchingEngine();
        engine.submitOrder(limit("resting", OrderSide.SELL, "100.00", "1", 1));

        MatchResult result = engine.submitOrder(limit("incoming", OrderSide.BUY, "100.00", "3", 2));

        assertEquals(1, result.trades().size());
        assertEquals(new BigDecimal("1"), result.trades().get(0).quantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, result.incomingOrder().getStatus());
        assertEquals(new BigDecimal("2"), result.incomingOrder().getRemainingQuantity());
        // unfilled remainder should now rest on the book as the new best bid
        assertEquals(new BigDecimal("100.00"), engine.getBook("BTC-USD").bestBid());
    }

    @Test
    void partiallyFillsWhenRestingQuantityExceedsIncoming() {
        MatchingEngine engine = new MatchingEngine();
        Order resting = limit("resting", OrderSide.SELL, "100.00", "5", 1);
        engine.submitOrder(resting);

        MatchResult result = engine.submitOrder(limit("incoming", OrderSide.BUY, "100.00", "2", 2));

        assertEquals(1, result.trades().size());
        assertEquals(new BigDecimal("2"), result.trades().get(0).quantity());
        assertEquals(OrderStatus.FILLED, result.incomingOrder().getStatus());
        assertEquals(OrderStatus.PARTIALLY_FILLED, resting.getStatus());
        assertEquals(new BigDecimal("3"), resting.getRemainingQuantity());
        // resting order should still be on the book with its remaining quantity
        assertEquals(new BigDecimal("100.00"), engine.getBook("BTC-USD").bestAsk());
    }

    @Test
    void ordersAtSamePriceFillInTimePriorityOrder() {
        MatchingEngine engine = new MatchingEngine();
        engine.submitOrder(limit("first", OrderSide.SELL, "100.00", "1", 1));
        engine.submitOrder(limit("second", OrderSide.SELL, "100.00", "1", 2));

        MatchResult result = engine.submitOrder(limit("incoming", OrderSide.BUY, "100.00", "1", 3));

        assertEquals(1, result.trades().size());
        assertEquals("first", result.trades().get(0).makerOrderId());
        // "second" should still be resting, "first" should be gone
        assertEquals(new BigDecimal("100.00"), engine.getBook("BTC-USD").bestAsk());
        assertEquals(1, engine.getBook("BTC-USD").getLevels(OrderSide.SELL).get(0).totalQuantity().intValue());
    }

    @Test
    void betterPricedOrderFillsBeforeWorsePricedOrder() {
        MatchingEngine engine = new MatchingEngine();
        engine.submitOrder(limit("worse", OrderSide.SELL, "101.00", "1", 1));
        engine.submitOrder(limit("better", OrderSide.SELL, "100.00", "1", 2));

        MatchResult result = engine.submitOrder(limit("incoming", OrderSide.BUY, "101.00", "1", 3));

        assertEquals(1, result.trades().size());
        assertEquals("better", result.trades().get(0).makerOrderId());
        assertEquals(new BigDecimal("100.00"), result.trades().get(0).price());
    }

    @Test
    @Disabled("Self-trade policy not yet decided/implemented (currently allows self-trades)")
    void handlesSelfTradeAccordingToChosenPolicy() {
    }

    @Test
    void marketOrderSweepsMultiplePriceLevels() {
        MatchingEngine engine = new MatchingEngine();
        engine.submitOrder(limit("level1", OrderSide.SELL, "100.00", "1", 1));
        engine.submitOrder(limit("level2", OrderSide.SELL, "101.00", "1", 2));

        MatchResult result = engine.submitOrder(market("incoming", OrderSide.BUY, "2", 3));

        assertEquals(2, result.trades().size());
        assertEquals(new BigDecimal("100.00"), result.trades().get(0).price());
        assertEquals(new BigDecimal("101.00"), result.trades().get(1).price());
        assertEquals(OrderStatus.FILLED, result.incomingOrder().getStatus());
        assertNull(engine.getBook("BTC-USD").bestAsk());
    }

    @Test
    void cancelledOrderNoLongerMatches() {
        MatchingEngine engine = new MatchingEngine();
        engine.submitOrder(limit("resting", OrderSide.SELL, "100.00", "1", 1));

        assertTrue(engine.cancelOrder("BTC-USD", "resting"));
        assertFalse(engine.cancelOrder("BTC-USD", "resting")); // already cancelled

        MatchResult result = engine.submitOrder(limit("incoming", OrderSide.BUY, "100.00", "1", 2));

        assertTrue(result.trades().isEmpty());
        assertEquals(OrderStatus.NEW, result.incomingOrder().getStatus());
        assertEquals(new BigDecimal("100.00"), engine.getBook("BTC-USD").bestBid());
    }
}