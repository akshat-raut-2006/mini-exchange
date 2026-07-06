package com.matchingengine.api.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

/** Plain row mapping for the `orders` table (see schema.sql). */
public record OrderRow(
        String id,
        String symbol,
        String side,
        String type,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal remainingQuantity,
        String status,
        Instant createdAt
) {
}
