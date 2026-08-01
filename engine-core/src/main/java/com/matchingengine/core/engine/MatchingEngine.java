package com.matchingengine.core.engine;
 
import com.matchingengine.core.book.OrderBook;
import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.OrderSide;
import com.matchingengine.core.model.OrderType;
import com.matchingengine.core.model.Trade;
 
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
     *
     * Self-trade policy: not yet implemented — an incoming order can match
     * against a resting order with the same origin. Revisit before going
     * anywhere near production; see MatchingEngineTest#handlesSelfTradeAccordingToChosenPolicy.
     */
    public MatchResult submitOrder(Order order) {
        OrderBook book = getBook(order.getSymbol());
        OrderSide oppositeSide = order.getSide() == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
        List<Trade> trades = new ArrayList<>();
 
        while (order.getRemainingQuantity().signum() > 0) {
            Order maker = book.peekBest(oppositeSide);
            if (maker == null) {
                break;
            }
            if (order.getType() == OrderType.LIMIT && !pricesCross(order, maker)) {
                break;
            }
 
            BigDecimal tradeQty = order.getRemainingQuantity().min(maker.getRemainingQuantity());
            BigDecimal tradePrice = maker.getPrice(); // resting order's price is the trade price
 
            order.reduceRemaining(tradeQty);
            maker.reduceRemaining(tradeQty);
            book.syncAfterMatch(maker);
 
            // Trade IDs must be globally unique across restarts, not just within
            // one run — the database persists across server restarts but an
            // in-memory counter does not, so a counter-based ID (e.g. "T-1")
            // will collide with a row from a previous run and fail the insert.
            trades.add(new Trade(
                    "T-" + UUID.randomUUID(),
                    order.getSymbol(),
                    maker.getId(),
                    order.getId(),
                    tradePrice,
                    tradeQty,
                    Instant.now()));
        }
 
        if (order.getRemainingQuantity().signum() > 0 && order.getType() == OrderType.LIMIT) {
            book.addOrder(order);
        }
 
        return new MatchResult(order, trades);
    }
 
    /** Cancel a resting order. Returns true if found and cancelled. */
    public boolean cancelOrder(String symbol, String orderId) {
        OrderBook book = getBook(symbol);
        Order order = book.getOrder(orderId);
        if (order == null) {
            return false;
        }
        boolean removed = book.cancelOrder(orderId);
        if (removed) {
            order.markCancelled();
        }
        return removed;
    }
 
    /** Get (or lazily create) the book for a symbol. */
    public OrderBook getBook(String symbol) {
        return booksBySymbol.computeIfAbsent(symbol, OrderBook::new);
    }
 
    /** True if the incoming order's price is willing to trade against the resting maker's price. */
    private static boolean pricesCross(Order incoming, Order maker) {
        return incoming.getSide() == OrderSide.BUY
                ? incoming.getPrice().compareTo(maker.getPrice()) >= 0
                : incoming.getPrice().compareTo(maker.getPrice()) <= 0;
    }
}
 