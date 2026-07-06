package com.matchingengine.api.config;

import com.matchingengine.api.websocket.OrderBookWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderBookWebSocketHandler handler;

    public WebSocketConfig(OrderBookWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // TODO(person-B): confirm path with dashboard/js/app.js, add setAllowedOrigins
        // appropriately once this isn't just running on localhost.
        registry.addHandler(handler, "/ws/orderbook").setAllowedOrigins("*");
    }
}
