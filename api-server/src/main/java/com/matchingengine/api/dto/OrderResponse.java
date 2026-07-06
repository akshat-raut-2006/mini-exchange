package com.matchingengine.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String orderId,
        String status,
        BigDecimal remainingQuantity,
        List<TradeDto> trades
) {
    public record TradeDto(
            String tradeId,
            BigDecimal price,
            BigDecimal quantity
    ) {
    }
}
