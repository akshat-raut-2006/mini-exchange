package com.matchingengine.core.engine;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

    @Test
    @Disabled("TODO: implement")
    void fullyMatchesEqualQuantityLimitOrders() {
    }

    @Test
    @Disabled("TODO: implement")
    void partiallyFillsWhenIncomingQuantityExceedsResting() {
    }

    @Test
    @Disabled("TODO: implement")
    void partiallyFillsWhenRestingQuantityExceedsIncoming() {
    }

    @Test
    @Disabled("TODO: implement")
    void ordersAtSamePriceFillInTimePriorityOrder() {
    }

    @Test
    @Disabled("TODO: implement")
    void betterPricedOrderFillsBeforeWorsePricedOrder() {
    }

    @Test
    @Disabled("TODO: implement - decide and document self-trade policy first")
    void handlesSelfTradeAccordingToChosenPolicy() {
    }

    @Test
    @Disabled("TODO: implement")
    void marketOrderSweepsMultiplePriceLevels() {
    }

    @Test
    @Disabled("TODO: implement")
    void cancelledOrderNoLongerMatches() {
    }
}
