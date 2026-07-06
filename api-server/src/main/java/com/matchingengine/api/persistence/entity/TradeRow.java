package com.matchingengine.api.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

/** Plain row mapping for the `trades` table (see schema.sql). */
public record TradeRow(
        String tradeId,
        String symbol,
        String makerOrderId,
        String takerOrderId,
        BigDecimal price,
        BigDecimal quantity,
        Instant executedAt
) {
}
