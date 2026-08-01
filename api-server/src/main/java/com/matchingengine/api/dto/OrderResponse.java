package com.matchingengine.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderId,
        String status,
        BigDecimal remainingQuantity,
        List<TradeDto> trades
) {
    public record TradeDto(
            String tradeId,
            String symbol,
            BigDecimal price,
            BigDecimal quantity,
            Instant executedAt
    ) {
    }
}