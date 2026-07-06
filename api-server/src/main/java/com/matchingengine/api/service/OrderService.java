package com.matchingengine.api.service;

import com.matchingengine.api.dto.BookSnapshotResponse;
import com.matchingengine.api.dto.OrderRequest;
import com.matchingengine.api.dto.OrderResponse;
import com.matchingengine.api.persistence.repository.OrderRepository;
import com.matchingengine.api.persistence.repository.TradeRepository;
import com.matchingengine.api.websocket.OrderBookWebSocketHandler;
import com.matchingengine.core.engine.MatchingEngine;
import org.springframework.stereotype.Service;

/**
 * Orchestrates a single request end to end:
 *   1. translate the DTO into an engine-core Order
 *   2. submit it to the MatchingEngine
 *   3. persist the resulting order + any trades
 *   4. broadcast the updated book over WebSocket
 *
 * TODO(person-B): this is the "connect real API to real engine" deliverable
 * for weeks 6-7. Keep translation logic (DTO <-> domain model) here rather
 * than in the controller, so the controller stays thin.
 */
@Service
public class OrderService {

    private final MatchingEngine matchingEngine;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final OrderBookWebSocketHandler webSocketHandler;

    public OrderService(MatchingEngine matchingEngine,
                         OrderRepository orderRepository,
                         TradeRepository tradeRepository,
                         OrderBookWebSocketHandler webSocketHandler) {
        this.matchingEngine = matchingEngine;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.webSocketHandler = webSocketHandler;
    }

    public OrderResponse submitOrder(OrderRequest request) {
        throw new UnsupportedOperationException("TODO: build Order, call engine.submitOrder, persist, broadcast");
    }

    public boolean cancelOrder(String symbol, String orderId) {
        throw new UnsupportedOperationException("TODO: call engine.cancelOrder, update persistence, broadcast");
    }

    public BookSnapshotResponse getBookSnapshot(String symbol) {
        throw new UnsupportedOperationException("TODO: read current levels from the engine's OrderBook");
    }
}
