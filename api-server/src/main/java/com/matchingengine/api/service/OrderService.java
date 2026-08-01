package com.matchingengine.api.service;

import com.matchingengine.api.dto.BookSnapshotResponse;
import com.matchingengine.api.dto.OrderRequest;
import com.matchingengine.api.dto.OrderResponse;
import com.matchingengine.api.persistence.entity.OrderRow;
import com.matchingengine.api.persistence.entity.TradeRow;
import com.matchingengine.api.persistence.repository.OrderRepository;
import com.matchingengine.api.persistence.repository.TradeRepository;
import com.matchingengine.api.websocket.OrderBookWebSocketHandler;
import com.matchingengine.core.book.PriceLevel;
import com.matchingengine.core.engine.MatchResult;
import com.matchingengine.core.engine.MatchingEngine;
import com.matchingengine.core.model.Order;
import com.matchingengine.core.model.OrderSide;
import com.matchingengine.core.model.OrderType;
import com.matchingengine.core.model.Trade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final MatchingEngine matchingEngine;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final OrderBookWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Every order needs a unique, ever-increasing sequence number so the
    // engine can break ties between orders at the same price (time priority).
    private final AtomicLong sequenceGenerator = new AtomicLong(0);

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
        Order order = new Order(
                UUID.randomUUID().toString(),
                request.symbol(),
                OrderSide.valueOf(request.side()),
                OrderType.valueOf(request.type()),
                request.price(),
                request.quantity(),
                sequenceGenerator.incrementAndGet()
        );

        MatchResult result = matchingEngine.submitOrder(order);

        persistOrderAndTrades(result);
        broadcastBookUpdate(request.symbol());

        return toOrderResponse(result);
    }

    public boolean cancelOrder(String symbol, String orderId) {
        boolean cancelled = matchingEngine.cancelOrder(symbol, orderId);
        if (cancelled) {
            broadcastBookUpdate(symbol);
        }
        return cancelled;
    }

    public BookSnapshotResponse getBookSnapshot(String symbol) {
        List<PriceLevel> bidLevels = matchingEngine.getBook(symbol).getLevels(OrderSide.BUY);
        List<PriceLevel> askLevels = matchingEngine.getBook(symbol).getLevels(OrderSide.SELL);

        return new BookSnapshotResponse(
                symbol,
                bidLevels.stream()
                        .map(l -> new BookSnapshotResponse.Level(l.getPrice(), l.totalQuantity()))
                        .toList(),
                askLevels.stream()
                        .map(l -> new BookSnapshotResponse.Level(l.getPrice(), l.totalQuantity()))
                        .toList()
        );
    }

    private void persistOrderAndTrades(MatchResult result) {
        Order o = result.incomingOrder();
        orderRepository.save(new OrderRow(
                o.getId(), o.getSymbol(), o.getSide().name(), o.getType().name(),
                o.getPrice(), o.getQuantity(), o.getRemainingQuantity(),
                o.getStatus().name(), o.getCreatedAt()
        ));

        for (Trade t : result.trades()) {
            tradeRepository.save(new TradeRow(
                    t.tradeId(), t.symbol(), t.makerOrderId(), t.takerOrderId(),
                    t.price(), t.quantity(), t.executedAt()
            ));
        }
    }

    private void broadcastBookUpdate(String symbol) {
        try {
            BookSnapshotResponse snapshot = getBookSnapshot(symbol);
            webSocketHandler.broadcast(objectMapper.writeValueAsString(snapshot));
        } catch (Exception e) {
            // A broadcast failure shouldn't fail the HTTP request that triggered it.
        }
    }

    private OrderResponse toOrderResponse(MatchResult result) {
        Order o = result.incomingOrder();
        List<OrderResponse.TradeDto> tradeDtos = result.trades().stream()
                .map(t -> new OrderResponse.TradeDto(t.tradeId(), t.symbol(), t.price(), t.quantity(), t.executedAt()))
                .toList();
        return new OrderResponse(o.getId(), o.getStatus().name(), o.getRemainingQuantity(), tradeDtos);
    }
}
