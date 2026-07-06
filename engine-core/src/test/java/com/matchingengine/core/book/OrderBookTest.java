package com.matchingengine.core.book;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * TODO(person-A): flesh these out as OrderBook is implemented. Suggested
 * cases beyond the ones stubbed below: adding multiple orders at the same
 * price preserves FIFO order; cancelling the only order at a level removes
 * the level entirely; best bid/ask update correctly as levels are added/removed.
 */
class OrderBookTest {

    @Test
    @Disabled("TODO: implement once OrderBook.addOrder works")
    void addingOrderMakesItBestBidOrAsk() {
    }

    @Test
    @Disabled("TODO: implement once OrderBook.cancelOrder works")
    void cancellingOrderRemovesItFromBook() {
    }

    @Test
    @Disabled("TODO: implement")
    void ordersAtSamePriceMaintainFifoOrder() {
    }

    @Test
    @Disabled("TODO: implement")
    void emptyBookHasNullBestBidAndAsk() {
    }
}
