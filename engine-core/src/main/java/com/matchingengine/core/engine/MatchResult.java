package com.matchingengine.core.engine;

import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.Trade;

import java.util.List;

/**
 * Result of submitting an order to the engine: any trades that occurred,
 * plus the (possibly still-resting) state of the incoming order.
 */
public record MatchResult(
        Order incomingOrder,
        List<Trade> trades
) {
}
