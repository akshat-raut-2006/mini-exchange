package com.matchingengine.api.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Broadcasts order book snapshots/deltas to all connected dashboard clients.
 *
 * TODO(person-B):
 *  - decide snapshot-per-change vs. true delta messages (snapshot is much
 *    simpler and fine for the dashboard's scale)
 *  - call broadcast(...) from OrderService after every successful
 *    submit/cancel that changes the book
 *  - pick a JSON shape and document it (e.g. { bids: [...], asks: [...],
 *    lastTrade: {...} })
 */
@Component
public class OrderBookWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        // TODO: send current book snapshot immediately on connect
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
    }

    /** Call this whenever the book changes to push an update to all clients. */
public void broadcast(String jsonPayload) {
    TextMessage message = new TextMessage(jsonPayload);
    for (WebSocketSession session : sessions) {
        if (session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                // A single slow/dead client shouldn't break the broadcast for everyone else.
                // afterConnectionClosed will clean it up from `sessions` shortly anyway.
            }
        }
    }
}
    }

