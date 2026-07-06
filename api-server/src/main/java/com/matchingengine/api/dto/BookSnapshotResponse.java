package com.matchingengine.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookSnapshotResponse(
        String symbol,
        List<Level> bids,
        List<Level> asks
) {
    public record Level(BigDecimal price, BigDecimal totalQuantity) {
    }
}
