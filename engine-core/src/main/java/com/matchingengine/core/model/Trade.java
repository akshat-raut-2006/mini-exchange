package com.matchingengine.core.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Result of a match between a resting (maker) order and an incoming
 * (taker) order.
 */
public record Trade(
        String tradeId,
        String symbol,
        String makerOrderId,
        String takerOrderId,
        BigDecimal price,
        BigDecimal quantity,
        Instant executedAt
) {
}
