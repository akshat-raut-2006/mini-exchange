package com.matchingengine.api.dto;

import java.math.BigDecimal;

/**
 * Request body for POST /api/orders.
 *
 * TODO(person-B): add bean validation annotations (@NotBlank, @NotNull,
 * @Positive) once the dependency is confirmed available, plus a
 * @ControllerAdvice to turn validation failures into clean 400 responses.
 */
public record OrderRequest(
        String symbol,
        String side,      // "BUY" | "SELL"
        String type,       // "LIMIT" | "MARKET"
        BigDecimal price,  // null for MARKET
        BigDecimal quantity
) {
}
